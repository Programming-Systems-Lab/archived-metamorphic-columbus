/*
	Copyright (c) 2007 Johannes Rieken, All Rights Reserved
	
	This file is part of Modern Jass (http://modernjass.sourceforge.net/).
	
	Modern Jass is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	Modern Jass is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public License
	along with Modern Jass.  If not, see <http://www.gnu.org/licenses/>.
*/
package jass.modern.core.compile;

import static jass.modern.core.compile.ContractJavaCompiler.CONTRACT_JAR;
import static jass.modern.core.util.Debug.log;
import jass.modern.core.util.CacheMap;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

/**
 * A file manager for the {@link ContractJavaCompiler}. This
 * file manager is a hybrid of a {@link StandardJavaFileManager} 
 * and a <i>Memory-Only</i>FileManager. 
 * <br /> <br />
 * This file manager can handle three different sources
 * for {@link JavaFileObject}s.
 * <ol>
 * <li>An in-memory cache for fresh class and source files.
 * 	When caching and fs-use is disabled or not possible,
 *  this in-memory data source is used. However, it means
 *  that (contract)-sources must be compiled first.
 * <li>The <code>_contracts.jar</code> file is queried for
 * 	contract class files. This jar is used to store class
 * 	files which have been generated during compilation.
 * <li>All entries from the (system)-classpath. This source
 * 	is used to access the runtime library and all external
 * 	libaries.
 * </ol>
 *
 * @author riejo
 */
public class ContractJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	/**
	 * Locations for the bytecode of contract classes.
	 *
	 * @author riejo
	 */
	public enum ContractLocations implements Location {
		
		/**
		 * Contracts are stored in a jar-file so
		 * they can be access easily later on.
		 */
		CONTRACT_JAR, 
		
		/**
		 * Contracts in memory only.
		 */
		CONTRACT_CACHE;

		@Override
		public String getName() {
			return "CONTRACT_LOCATIONS";
		}

		@Override
		public boolean isOutputLocation() {
			switch(this){
				case CONTRACT_JAR: return true;
				default:
					return false;
			}
		}
	}
	
	/**
	 * A {@link ContractJavaFile} that is read from a jar-file
	 * Works for binaries only.
	 *
	 * @author riejo
	 */
	private class ArchiveJavaFileForInput extends ContractJavaFile  {
		
		private JarFile fJarFile;
		
		protected ArchiveJavaFileForInput(JarFile in, String name) {
			super(name);
			fJarFile = in;
		}

		public boolean isValid() {
			return fJarFile.getEntry(super.getName()) != null;
		}
		
		@Override
		public InputStream openInputStream() throws IOException {
			JarEntry entry = fJarFile.getJarEntry(super.getName());
			
			return fJarFile.getInputStream(entry);
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * A {@link ContractJavaFile} that is written to a jar-file.
	 * Works for binaries only.
	 *
	 * @author riejo
	 */
	private class ArchiveJavaFileForOutput extends ContractJavaFile {

		private JarOutputStream fOut;
		
		public ArchiveJavaFileForOutput(JarOutputStream out, String name) {
			super(name);
			fOut = out;
		}

		@Override
		public InputStream openInputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			JarEntry entry = new JarEntry(super.getName());
			fOut.putNextEntry(entry);
			
			return new FilterOutputStream(fOut) {

				@Override
				public void close() throws IOException {
					fOut.closeEntry();
				}
			};
		}
	}
	
	private CacheMap<String, ContractJavaFile> fCacheSource = new CacheMap<String, ContractJavaFile>(75);
	
	private CacheMap<String, ContractJavaFile> fCacheClass = new CacheMap<String, ContractJavaFile>(75);
	
	private boolean fUseFileSystem = false;
	
	private JarOutputStream fJarOut;
	
	private JarFile fJarIn;
	
	/**
	 * 
	 * @param fileManager
	 */
	public ContractJavaFileManager(StandardJavaFileManager fileManager) {
		super(fileManager);
		useFileSystem(true);
		initContractJarFile();
	}

	/**
	 * Find the contract jar file (<code>_contracts.jar</code>)
	 * the classpath.
	 */
	protected void initContractJarFile() {
		Iterable<? extends File> path = fileManager.getLocation(
				StandardLocation.CLASS_PATH);
		
		if(path == null)
			return;
		
		for (File entry : path) {
			
			if(!entry.isDirectory())
				continue;
			
			File tmp = new File(entry + File.separator + CONTRACT_JAR);
			if(tmp.exists()) {
				try {
					fJarIn = new JarFile(tmp.getAbsoluteFile(), false, 
							JarFile.OPEN_READ);
					break;
					
				} catch (IOException e) {
					e.printStackTrace(); // XXX debug message
					fJarIn = null; 
				}
				
			}
		}
	}

	public void setArchiveOutputStream(OutputStream out, Manifest man) 
	throws IOException {
		
		fJarOut = new JarOutputStream(out, man);
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		
		JavaFileObject file;
		
  		/*
  		 * (1st) try to read from _contracts.jar
  		 */
		if(canReadContractFromJar() && location == ContractLocations.CONTRACT_JAR 
				&& kind == Kind.CLASS) {
			
			log().info("READING FROM ARCHIVE: " + className);
			
			file = new ArchiveJavaFileForInput(fJarIn, className);
			if(( (ArchiveJavaFileForInput) file).isValid()) {

				return file;
			}
		}
		
		/*
		 * (2nd) try to read from cache (previously compiled here)
		 */
		file = fromCache(className, kind);
		if(file != null) {
			return file;
		}
		
		/*
		 * (3rd) check the classpath
		 */
		return super.getJavaFileForInput(location, className, kind);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className,
			Kind kind, FileObject sibling) throws IOException {
		
		if(canWriteContractsToJar() && kind == Kind.CLASS) {
			log().info("WRITING TO ARCHIVE: " + className);
			return new ArchiveJavaFileForOutput(fJarOut, className);
		}
		
		JavaFileObject file = fromCache(className, kind);
		
		if(file != null) {
			return file;

		} else if(kind == Kind.CLASS) {
			ContractJavaFile tmp =  new ContractJavaFile(className);
			fCacheClass.put(className, tmp);
			return tmp;
		} 
		
		return null;
	}

	
	public Set<File> getLocation(Location location){
		HashSet<File> locations = new HashSet<File>();
		Iterable<? extends File> path = fileManager.getLocation(location);
		
		if(path != null) {
			
			for (File entry : path) {
				locations.add(entry);
			}
		}
		
		return locations;
	}
	
	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		fileManager.setLocation(location, path);
	}
	
	/**
	 * <b>Warning:</b> This method relies sololy on its internal
	 * cache. In other terms, if caching is {@link #disableCaching() 
	 * disabled}, this method will return <code>null</code> always.
	 * 
	 * @param className
	 * @param kind
	 * @return
	 */
	private ContractJavaFile fromCache(String className, Kind kind) {
		switch(kind) {
		case SOURCE:	return fCacheSource.get(className);
		case CLASS:		return fCacheClass.get(className);
		
		default:	return null;
		}
	}

	@Override
	public void close() throws IOException {
		fCacheSource.clear();
		fCacheClass.clear();
		
		fJarOut.flush();
		fJarOut.finish();
		fJarOut.close();
		
		super.close();
	}

	void disableCaching() {
		fCacheClass.setEnabled(false);
		fCacheSource.setEnabled(false);
	}
	
	void enableCaching() {
		fCacheClass.setEnabled(true);
		fCacheClass.setEnabled(true);
	}

	/**
	 * Enable or disable file access when
	 * looking for contract classes.
	 * @param useFS
	 */
	public void useFileSystem(boolean useFS) {
		fUseFileSystem = useFS;
	}

	boolean canWriteContractsToJar() {
		return fUseFileSystem && fJarOut != null;
	}
	
	boolean canReadContractFromJar() {
		return fUseFileSystem && fJarIn != null;
	}

	/*
	 * For debugging only
	 */
	void printLocations() {
		for(StandardLocation l : StandardLocation.values()) {
			System.out.println("[LOCATION] " + l);
			Iterable<? extends File> file = fileManager.getLocation(l);
			
			if(file == null) 
				continue;
			
			for (File f : file) {
				System.out.println("\t[FILE] " + f);
			}
		}
	}
}

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
package jass.modern.core.bytecode;

import jass.modern.core.bytecode.contracts.ContractClassFileTransformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The PreMain class which controls the {@link ContractClassFileTransformer}.
 * Has build-in support for a name based filter. 
 * E.g. <code>jass.modern.test.*</code> includes every class which 
 * name starts with <code>jass.modern.test.</code>. <em>Note</em> the difference
 * form the Java import statement which would include members of the package
 * jass.modern.test only. Filter strings must be separated with an comma.
 * E.g. <code>jass.modern.test.*,jass.modern.Spec</code>If no filter is 
 * specified all classes will be included.<br />
 * 
 * <em>Important note: </em> All classes which start with
 * <code>jass.modern.core.bytecode</code> are silently ignored by this
 * PreMain class.
 * 
 * @author riejo
 */
public class PreMain {
	
	private static final String OPT_SEPARATOR = System.getProperty("path.separator");
	
	/**
	 * Tells Modern Jass to dump the instrumented class files onto 
	 * the disk (<code>&lt;user.home&gt;/MJ_DUMP/</code>) before 
	 * loading them into the JVM.
	 */
	private static final String OPT_DUMP = "dump";
	
	/**
	 * Tells Modern Jass not to use class file for contract types
	 * that have been generated during compilaton, but to compile
	 * contracts prior to bytecode instrumentation. 
	 * <br /><br />
	 * The switch <code>nofsuse</code> stands for <i>no file system use</i>.
	 */
	private static final String OPT_NOFSUSE = "nofsuse";
	
	public static void premain(String args, Instrumentation inst) {
		
		boolean dump = false;
		boolean nofsuse = false;
		List<String> names = new LinkedList<String>();
		
		if(args != null) {
			  names.addAll(Arrays.asList(args.split(OPT_SEPARATOR)));
			
			if(names.contains(OPT_NOFSUSE)) {
				nofsuse = true;
				names.remove(OPT_NOFSUSE);
			}
			
			if(names.contains(OPT_DUMP)) {
				dump = true;
				names.remove(OPT_DUMP);
			}
		}
		
		ClassFileTransformer transformer = new FilteringTransformer(
						new DumpParentTransformer(
								new ContractClassFileTransformer(!nofsuse), dump), names);
		inst.addTransformer(transformer);
	}
	
	
	private static abstract class DelegatingClassFileTransformer implements ClassFileTransformer {
		
		protected ClassFileTransformer fParent;
		
		public DelegatingClassFileTransformer(ClassFileTransformer parent) {
			fParent = parent;
		}
	}
	
	
	private static class DumpParentTransformer extends DelegatingClassFileTransformer {

		protected boolean fDump = false;
		
		public DumpParentTransformer(ClassFileTransformer parent, boolean dump) {
			super(parent);
			fDump = dump;
		}
		
		@Override
		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			
			byte[] data = fParent.transform(loader, className, 
					classBeingRedefined, protectionDomain, classfileBuffer);
			
			if(fDump) {
				dump(className, data);
			}
			
			return data;
		}
		
		protected void dump(String name, byte[] data) {
			
			File f = new File(System.getProperty("user.home") + "/MJ_DUMP/" + 
					name.replace('.', '/') + ".class");
			f.getParentFile().mkdirs();
			try {
				OutputStream out = new FileOutputStream(f);
				out.write(data);
				out.flush();
				out.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 *
	 * @author riejo
	 */
	private static class FilteringTransformer extends DelegatingClassFileTransformer {

		protected List<String> fNames = new LinkedList<String>();
		
		public FilteringTransformer(ClassFileTransformer parent, List<String> names) {
			super(parent);
			fNames.addAll(names);
		}

		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			
			if(include(className)) {
				byte[] data = fParent.transform(loader, className, classBeingRedefined, 
						protectionDomain, classfileBuffer);
					
				return data;
				
			} else
				return classfileBuffer;
		}

		protected boolean include(String className) {
			className = className.replace('/', '.');
			
			if(className.startsWith("jass.modern.core.bytecode"))
				return false;
			
			if(fNames.isEmpty())
				return true;
			
			for (String name : fNames) {
				if(name.equals(className))
					return true;

				if(name.endsWith("*") && 
						className.startsWith(name.substring(0, name.length() - 1)))
					return true;
			}
			
			return false;
		}
		
	}
}

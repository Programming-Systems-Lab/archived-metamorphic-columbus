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

import jass.modern.SpecCase;
import jass.modern.core.apt.AnnotationProcessor;
import jass.modern.core.bytecode.contracts.ContractClassFileTransformer;
import jass.modern.core.compile.creation.AnnotationUsageValidator;
import jass.modern.core.compile.creation.ContractCreationController;
import jass.modern.core.compile.creation.ModelVariableHelper;
import jass.modern.core.compile.creation.ModelVariableValidator;
import jass.modern.core.compile.desugar.DesugaringLevel1Visitor;
import jass.modern.core.compile.desugar.DesugaringLevel2Visitor;
import jass.modern.core.model.IElementVisitor;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.DecoratedType;
import jass.modern.core.model.impl.Type;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.CacheMap;
import jass.modern.core.util.JavaUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import com.sun.mirror.declaration.TypeDeclaration;

/**
 * The <em>Contract</em> Java compiler takes a type
 * and creates and compilates the contract of that type.
 * Any compilation problem is reported to a {@link DiagnosticCollector}
 * which is passed to the <code>getCompilationTask(...)</code>-methods.
 * <br />
 * For instance, for the type <code>CU</code>
 * <pre>
 * class CU {
 * 	
 * 	&#064;Pre("o != null")
 * 	void m(Object o){
 * 
 * 	}
 *
 * }
 * </pre>
 * The compilation unit <code>CU$Contract</code> is created
 * <pre>
 * abstract class CU {
 * 
 * 	abstract void m(Object o);
 * 
 * 	boolean m$pre$1(Object o){
 * 		return o != null;
 * 	}
 * }
 * </pre>
 * 
 * To compile this unit, the {@linkplain IExtendedCompilationTask#run} method
 * needs to be called. (Alternatively {@linkplain IExtendedCompilationTask#getResult}
 * will work as well.)
 * @see AnnotationProcessor
 * @see ContractClassFileTransformer
 * 
 * @author riejo
 */
@SuppressWarnings("unchecked")
public class ContractJavaCompiler {
	
	/**
	 * The name of jar-file which stores contract files.
	 */
	public static final String CONTRACT_JAR = "_contracts.jar";
	
	/**
	 * The compiler options that are used for the internal
	 * compiler.
	 */
	protected static final LinkedList<String> OPTIONS = new LinkedList<String>(
			Arrays.asList(
//			"-verbose",		// print to System.err what the compiler is doing
			"-g:none", 		// no debug info in contract code
			"-nowarn",		// no warnings ala 'redundant cast' etc.
			"-proc:none"	// prevent from re-processing the contract annotations...
			));
	
	/**
	 * Delegate for the actual compilation process.
	 */
	private JavaCompiler fJavaCompiler = ToolProvider.getSystemJavaCompiler();
	
	private ContractJavaFileManager fFileManager = new ContractJavaFileManager(
			fJavaCompiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset()));
	
	private TypeFactory fTypeFactory = new TypeFactory(false);
	
	private ModelVariableHelper fModelVariableHelper = new ModelVariableHelper();
	
	/**
	 * The element visitors which transform {@link IType}s into
	 * contract-types.
	 */
	private IElementVisitor<DiagnosticListener<JavaFileObject>>[] fVisitors; 
	
	private CacheMap<String, DecoratedType> fCache = new CacheMap<String, DecoratedType>(75);
	
	/**
	 * Interally maintained classloader which is used to 
	 * loaded classes from the custom classpath
	 * 
	 * @see #addClasspathEntry(String)
	 * @see #updateInternalClassLoader(String)
	 * @see #openStream(String)
	 */
	private URLClassLoader fInternalClassLoader = URLClassLoader.newInstance(
			new URL[0], ContractJavaCompiler.this.getClass().getClassLoader());

	
	/**
	 * The singleton instance.
	 */
	private static ContractJavaCompiler instance;

	/**
	 * 
	 * @return Returns the singleton instance of this {@link ContractJavaCompiler}.
	 */
	public static ContractJavaCompiler getInstance() {
		if(instance == null) {
			instance = new ContractJavaCompiler();
			
			/*
			 * this is a bit dirty - the array must 
			 * be initialized in the static part
			 * of this class because the element
			 * visitor below reference this class,
			 * otherwise endless recursion happens.
			 */
			instance.fVisitors = new IElementVisitor[] {
				new AnnotationUsageValidator(),
				new DesugaringLevel1Visitor(),
				new DesugaringLevel2Visitor(),
				new ContractCreationController(),
				new ModelVariableValidator()
			};
		}
		
		return instance;
	}
	
	protected ContractJavaCompiler() {
		
		enableCaching();
	}
	
	public IExtendedCompilationTask getCompilationTask(byte[] data, 
			DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
		
		return getCompilationTask(new ByteArrayInputStream(data), diagnostics);
	}
	
	public IExtendedCompilationTask getCompilationTask(String name, 
			DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
		
		InputStream in = openStream(name);
		if(in == null)
			return null;
		
		return getCompilationTask(in, diagnostics);
	}

	public IExtendedCompilationTask getCompilationTask(InputStream in, 
				DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
		
		IType type = fTypeFactory.createType(in);
		return getCompilationTask(type, diagnostics);
	}
	
	public IExtendedCompilationTask getCompilationTask(IType type, 
				DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
		
		DecoratedType dType = createContractCode(type, diagnostics);
		JavaFileObject cu = new ContractJavaFile(dType);
		
		CompilationTask task = fJavaCompiler.getTask(null, fFileManager, diagnostics, 
				OPTIONS, null, Arrays.asList(cu));
		
		return new CompilationTaskDecorator(task, dType.hasErrors(), dType);
	}

	/**
	 * Get a compilation task from the passed collection of {@link TypeElement}s and
	 * the {@link DiagnosticCollector}.
	 * <br/>
	 * <em>Use this method when dealing with annotation processor later or
	 * equals to Java 6.
	 * <br/>
	 * 
	 * @param typesElements
	 * @param diagnostics
	 * @return
	 */
	public IExtendedCompilationTask getCompilationTaskJava6(Collection<TypeElement> typesElements, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		List<IType> types = new ArrayList<IType>(typesElements.size());
		List<ContractJavaFile> compilationUnits = new ArrayList<ContractJavaFile>(
				typesElements.size());
		
		boolean errors = false;
		
		for(TypeElement typeElement : typesElements) {
			IType type = fTypeFactory.createType(typeElement);
			type = createContractCode(type, diagnostics);
			errors |= ((DecoratedType) type).hasErrors();
			
			types.add(type);
			compilationUnits.add(new ContractJavaFile(type));
		}
		
		CompilationTask task = fJavaCompiler.getTask(null, fFileManager, diagnostics, 
				OPTIONS, null, compilationUnits);
		
		return new CompilationTaskDecorator(task, errors, types.toArray(new IType[types.size()]));
	}
	
	/**
	 * Get a compilation task from the passed collection of {@link TypeDeclaration}s
	 * and the {@link DiagnosticCollector}.
	 * <br/>
	 * <em>Use this method when dealing with annotation processor introduced
	 * in Java 5. However, the mirror api will be deprecated in near future.
	 * <br/>
	 * 
	 * @see #getCompilationTask(String, DiagnosticCollector)
	 * @param typeDeclarations
	 * @param diagnostics
	 * @return
	 */
	public IExtendedCompilationTask getCompilationTaskJava5(Collection<TypeDeclaration> typeDeclarations, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		List<IType> types = new ArrayList<IType>(typeDeclarations.size());
		List<ContractJavaFile> compilationUnits = new ArrayList<ContractJavaFile>(
				typeDeclarations.size());
		
		boolean errors = false;
		
		for (TypeDeclaration typeDeclaration : typeDeclarations) {
			IType type = fTypeFactory.createType(typeDeclaration);
			type = createContractCode(type, diagnostics);
			errors |= ((DecoratedType) type).hasErrors();
			
			types.add(type);
			compilationUnits.add(new ContractJavaFile(type));
		}
		
		CompilationTask task = fJavaCompiler.getTask(null, fFileManager, diagnostics, 
				OPTIONS, null, compilationUnits);
		
		return new CompilationTaskDecorator(task, errors, types.toArray(new IType[types.size()]));
	}
	
	/**
	 * This is where the magic happens.
	 * <ol>
	 * <li>Validate the annotations (see {@link AnnotationUsageValidator})
	 * <li>Desugar all super-leightweight and leightweight specifications
	 * 	into {@link SpecCase}s. 
	 * <li>Create the contract code.
	 * </ol>
	 * 
	 * @param type The curret type.
	 */
	protected DecoratedType createContractCode(IType type, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		DecoratedType dType = fCache.get(type.getQualifiedName());
		if(dType != null) {
			return dType;
		}
		
		/*ý
		 * create contract code
		 */
		for (IElementVisitor<DiagnosticListener<JavaFileObject>> visitor : fVisitors) {
			type.accept(visitor, diagnostics);
		}
		
		dType = new DecoratedType((Type) type, diagnostics.getDiagnostics());
		
		/*
		 * cache type with contract if it has no erros
		 */
		if(!dType.hasErrors()) {
			fCache.put(dType.getQualifiedName(), dType);
		}
		
		return dType;
	}

	/**
	 * Clear the cache and reset the classpath to its default value.
	 * <br/><br />
	 * <em>Note:</em> Resetting the {@link ContractJavaCompiler} is
	 * vital when Modern Jass is used in a IDE. Otherwise, changes
	 * of source code are 'overwritten' with older version from
	 * the cache.
	 */
	public void reset() {
		fCache.clear();
		
		/*
		 * create new compiler and file manager because of 
		 * classpath issues...
		 */
		fJavaCompiler = ToolProvider.getSystemJavaCompiler();
		try {
			
			fFileManager.close();
		} catch (IOException e) { e.printStackTrace(); }
		fFileManager = new ContractJavaFileManager(
				fJavaCompiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset()));
	}
	
	public void disableCaching() {
		fCache.setEnabled(false);
		fFileManager.disableCaching();
	}
	
	public void enableCaching() {
		fCache.setEnabled(true);
		fFileManager.enableCaching();
	}
	
	public ContractJavaFileManager getFileManager() {
		return fFileManager;
	}

	public ModelVariableHelper getModelVariableHelper() {
		return fModelVariableHelper;
	}

	public TypeFactory getTypeFactory() {
		return fTypeFactory;
	}
	
	public void setContractArchiveOutputStream(OutputStream out) throws IOException {
		Manifest man = new Manifest();
		man.getMainAttributes().put(new Attributes.Name("contracts"), "modernjass");
		fFileManager.setArchiveOutputStream(out, man);
	}

	/**
	 * Add an entry to the classpath of this compiler. By default, the
	 * entries returned by <code>System.getProperty("java.class.path")</code>
	 * are used.
	 * 
	 * @param entry A single or multiple classpath entries. If more 
	 * 	then one are passed, they separate them with {@link File#pathSeparator}.
	 * @throws IOException 
	 */
	public void addClasspathEntry(String entry) throws IOException {
		
		String classpath = getCurrentClasspath();
		
		classpath = classpath.concat(File.pathSeparator + entry);
		updateInternalClassLoader(classpath);
		
		String[] entries = classpath.split(File.pathSeparator);
		HashSet<File> path = new HashSet<File>();
		for (String tmp : entries) {
			path.add(new File(tmp));
		}

		fFileManager.setLocation(StandardLocation.CLASS_PATH, path);		
	}
	
	/**
	 * Returns the currently used classpath. Entries are
	 * separated by the {@link File#separator} character.
	 * 
	 * @return The classpath which is '<code>.</code>' at least
	 */
	public String getCurrentClasspath() {
		Set<File> path = fFileManager.getLocation(StandardLocation.CLASS_PATH);
		StringBuilder tmp = new StringBuilder(".");
		for (File file : path) {
			tmp.append(File.pathSeparator + file);
		}
		
		String classpath = tmp.toString();
		return classpath;
	}
	
	/**
	 * Updates the classpath of the interally used {@link URLClassLoader}.
	 * @see #addClasspathEntry(String)
	 * @param entry
	 */
	private void updateInternalClassLoader(String entry) {
		String[] entries = entry.split(File.pathSeparator);
		List<URL> urls = new ArrayList<URL>(entries.length);
		
		
		
//		XXX getURLs returns an empty array - bug in the java.net.URLClassloader implementation ?
//		urls.addAll(Arrays.asList(fInternalClassLoader.getURLs()));

		for (String element : entries) {
			try {
				//ensure dirs end with a slash
				element = element.endsWith(".jar") ? element : 
					element.endsWith("/") ? element : element + "/";
				
				URL url = new URL("file://"+element);
				urls.add(url);
				
			} catch (MalformedURLException e) {	
				throw new RuntimeException(e); 
			}
		}
		
		fInternalClassLoader = URLClassLoader.newInstance(
				urls.toArray(new URL[urls.size()]), 
				ContractJavaCompiler.this.getClass().getClassLoader());
	}
	
	/**
	 * Returns an {@link InputStream} offering the bytecodes 
	 * for the denoted class. 
	 * 
	 * @param className A fully qualified classname.
	 * @return An {@link InputStream} or <code>null</code>.
	 */
	public InputStream openStream(String className) {
		className = className.replace('.', '/') + ".class";
		InputStream in = ClassLoader.getSystemResourceAsStream(className);
		if(in != null)
			return in;
		
		in = fInternalClassLoader.getResourceAsStream(className);
		return in;
	}
	
	/**
	 * Similar to {@link Class#forName(String)} however it uses
	 * the interally used classloader and, thus, does not miss
	 * classes which are getting compiled currently. 
	 * 
	 * @see #fInternalClassLoader
	 * @see Class#forName(String, boolean, ClassLoader)
	 * @param className
	 * @return
	 */
	public Class<?> forName(String className){
		Class<?> cls = null;
		
		try {
			className = JavaUtil.fixJavaTypeName(className);
			cls = Class.forName(className, true, fInternalClassLoader);
			return cls;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

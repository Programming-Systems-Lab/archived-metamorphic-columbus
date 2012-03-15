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
package jass.modern.core.bytecode.contracts;

import jass.modern.core.bytecode.ModernJassAnnotationSearcher;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.ContractJavaFile;
import jass.modern.core.compile.ContractJavaFileManager;
import jass.modern.core.compile.IExtendedCompilationTask;
import jass.modern.core.compile.ContractJavaFileManager.ContractLocations;
import jass.modern.core.model.IType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * This transform is called by the Java agent which is reposible for
 * class file instrumentation. It will instrument classes so that 
 * contract checking is enabled. See 
 * {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])}
 * to get insights into the magic. Basicly the transformation process
 * works in 4 steps:
 * <ol>
 * <li>Compile contracts into bytecode
 * <li>Analye contracts and store them into a central repository ({@link ContractCodePool}).
 * <li>Repeat step 1 and 2 for all supertypes (classes and interfaces)
 * <li>Patch contracts into classfile.
 * </ol>
 * 
 * @author riejo
 */
public class ContractClassFileTransformer implements ClassFileTransformer {

	private ModernJassAnnotationSearcher fAnnotationSearcher = new ModernJassAnnotationSearcher();
	
	private ContractJavaCompiler fContractCompiler = ContractJavaCompiler.getInstance();
	
	private ContractJavaFileManager fFileManager = fContractCompiler.getFileManager();
	
	private Map<String, byte[]> fCache = new HashMap<String, byte[]>();
	
	private LinkedList<String> fHierachyStack = new LinkedList<String>();
	
	
	public ContractClassFileTransformer(boolean useFS) {
		fFileManager.useFileSystem(useFS);
	}
	
	/**
	 * Translates the passed class file so that contract annotation are 
	 * actually runnable bytecode during runtime. <br />
	 * E.g: From a method defined as:
	 * <pre>
	 * &#064;Spec(&#064;Case(pre="obj != null", preMsg="Pre-Condition violated"))
	 * public void add(Object obj){ 
	 * 	// ...code...
	 * }
	 * </pre>
	 * bytecode equivalent to
	 * <pre>
	 * public void add(Object obj){
	 * 	boolean pre1 = add$pre$1(obj);
	 * 	if(!pre1)
	 * 		throw new AssertionError("Pre-Condition violated");
	 * 
	 * 	//...code...
	 * }
	 * 
	 * &#064;ContractInfo(message = "Pre-Condition violated", code = "obj != null")
	 * public boolean add$pre$1(Object obj){
	 * 	return obj != null;
	 * }
	 * </pre>
	 * is created.
	 * 
	 * @see SpecificationMethodAdapter <i>Does the acutal bytecode instrumentation</i>
	 * @see ContractAnalyzer <i>Analyses the classfile and extracts contract information</i>
	 * @see ContractCodePool <i>Central repository for contracts</i>
	 */
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		// (1) check if any modern jass annotations are present
		if(!analyseType(classfileBuffer)) {
			return classfileBuffer;
		}
		
		// (2) perform transformations
		try {
			DiagnosticCollector<JavaFileObject> dianostics = new DiagnosticCollector<JavaFileObject>();
			byte[] data = recursiveTransform(className.replace('/', '.'), classfileBuffer, loader, 
					protectionDomain, dianostics);
		
			fHierachyStack.clear();
			
			return data;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}

	/**
	 * Ensures that all supertype are loaded, analyses, and instrumented 
	 * before proceeding with current type. Caches the results to speed
	 * up the whole process.
	 * @param className 
	 * @param classfileBuffer
	 * @param loader
	 * @param protectionDomain
	 * @return
	 * @throws IOException
	 * @throws IllegalClassFormatException
	 */
	private byte[] recursiveTransform(String className, byte[] classfileBuffer, 
			ClassLoader loader, ProtectionDomain protectionDomain, 
			DiagnosticCollector<JavaFileObject> diagnostics) throws IOException, IllegalClassFormatException {

		if(className.startsWith("java")) {
			/*
			 * skip everything that starts with 
			 * 'java' e.g java.lang.Object
			 */
			return classfileBuffer;
		}

		IType type = fContractCompiler.getTypeFactory().createType(className, classfileBuffer);
		
		// (1) the supertypes & interfaces go first
		String superClass = type.getSuperclass();
		if(!superClass.equals("java.lang.Object")) {
			
			byte[] data = recursiveTransform(superClass, getBytes(superClass), loader, 
					protectionDomain, diagnostics);
			
			fCache.put(superClass, data);
		}
		
		List<String> interfaces = type.getInterfaces();
		for(String _interface : interfaces) {
			
			byte[] data = recursiveTransform(_interface, getBytes(_interface), loader, 
					protectionDomain, diagnostics);
			
			fCache.put(_interface, data);
		}

		
		/*
		 * reflect the inheritance hierarchy of the roottype.
		 * ... | java.lang.String | java.lang.Object |
		 */
		fHierachyStack.push(className);
		
		JavaFileObject file = getContractJavaFile(className, diagnostics, type);
		
		/*
		 * 1st - check if class file is cached already
		 * 2nd - check if instrumentation required
		 * 3th - instrument class file
		 */
		byte[] cache = fCache.get(className);
		if(cache != null)
			return cache;
		
		boolean hasContracts = analyseContracts(file);
		if(!hasContracts && fHierachyStack.size() == 1) {
			return classfileBuffer;
			
		} else {
			return instrumentWithContract(classfileBuffer);
		}
	}

	
	/**
	 * Returns a {@link JavaFileObject} that represents the bytecode
	 * of a contract class. That is the bytecode of the source files
	 * which are generated in the background. E.g. the bytecode
	 * for the class
	 * <pre>
	 * class CU {
	 *  abstract String foo();
	 *  boolean foo$post$1(String _RETURN){ return _RETURN != null; } 
	 * }
	 * </pre>
	 * <br />
	 * This method tries to read the bytecode from 
	 * {@link ContractLocations#CONTRACT_JAR} first. If no bytecode 
	 * can be found, a new compilation task is created and '<i>fresh</i>'
	 * bytecode is generated.
	 * 
	 * @param className 
	 * @param diagnostics
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private JavaFileObject getContractJavaFile(String className,
			DiagnosticCollector<JavaFileObject> diagnostics, IType type)
			throws IOException {
		
		JavaFileObject file = fContractCompiler.getFileManager().getJavaFileForInput(
				ContractLocations.CONTRACT_JAR, className, Kind.CLASS);
		
		if(file == null) {
			IExtendedCompilationTask task = fContractCompiler.getCompilationTask(
					type, diagnostics);
	
			boolean success = task.call();
			
			if(success) {
				file = fContractCompiler.getFileManager().getJavaFileForInput(
						ContractLocations.CONTRACT_CACHE, className, Kind.CLASS);
				assert file != null;
					
			} else {
			
				System.out.println(new ContractJavaFile(
						task.getTypes().get(0)).getCharContent(true));
				
				for (Diagnostic<? extends JavaFileObject> diagnostic : 
					diagnostics.getDiagnostics()) {
					
					System.out.println(diagnostic.getSource() + ":" + diagnostic.getKind() + ":" + diagnostic.getMessage(null));
				}
				throw new RuntimeException(task.getTypes() + " can not be compiled.");
			}
		}
		
		return file;
	}

	/**
	 * Analyses the passed contract class and stores all information
	 * about contract method into the {@link ContractCodePool}.
	 *  
	 * @see ContractCodePool
	 * @see ContractAnalyzer
	 * 
	 * @param file The type with all contract information, e.g. 
	 * 	contract methods
	 * @return Returns <code>true</code> if at least one contract 
	 * 	method has been found.
	 * @throws IOException
	 */
	private boolean analyseContracts(JavaFileObject file)	throws IOException {
		ContractAnalyzer fAnalyser = new ContractAnalyzer();	
		ClassReader reader = new ClassReader(file.openInputStream());
		reader.accept(fAnalyser, ClassReader.EXPAND_FRAMES);
		
		return fAnalyser.getContractCount() > 0;
	}

	/**
	 * Instrument the passed class file (represented as a byte array) 
	 * so that it contains accordant contract methods and calls
	 * those methods. The contract information is gain through
	 * {@link ContractCodePool}.
	 * @see ContractCodePool
	 * 
	 * @param classfileBuffer The unmodified bytes of the class
	 * @return The bytes of the instrumented class.
	 */
	private byte[] instrumentWithContract(byte[] classfileBuffer) {
		ClassReader reader;
		
		LinkedList<String> typeHierarchy = new LinkedList<String>(fHierachyStack);
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | 
				ClassWriter.COMPUTE_MAXS);
		reader = new ClassReader(classfileBuffer);
		reader.accept(new SpecificationClassAdapter(writer, typeHierarchy), 
				ClassReader.EXPAND_FRAMES);
		
		
		return writer.toByteArray();
	}

	/**
	 * 
	 * 
	 * @param classfileBuffer
	 * @return Returns <code>true</code> iff the class file
	 * 	has any annotations with from the <code>jass.modern.*</code>
	 * 	package or subpackages.
	 */
	private boolean analyseType(byte[] classfileBuffer) {
		fAnnotationSearcher.reset();
		new ClassReader(classfileBuffer).accept(fAnnotationSearcher, 
				ModernJassAnnotationSearcher.FLAGS);
		
		return fAnnotationSearcher.hasModernJassAnnotations();
	}

	/**
	 * Reads the specified <code>.class</code>-file and returns
	 * its contents as byte array. Uses 
	 * {@link ClassLoader#getSystemResourceAsStream(String)} to
	 * read the file. 
	 * 
	 * @param className A fully qualified name of a class. E.g.
	 * 	<code>java.lang.String</code>
	 * @return The bytes of the specified class.
	 * @throws IOException If the accordant file can not be
	 * 	opened.
	 */
	public byte[] getBytes(String className) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		InputStream in = ClassLoader.getSystemResourceAsStream(
				className.replace('.', '/') + ".class");
		if(in == null)
			throw new IOException("Cannot open file for class: " + className);
		
		in = new BufferedInputStream(in);
		byte[] block = new byte[512];
		int len;
		while((len = in.read(block)) != -1) {
			buffer.write(block, 0, len);
		}
		
		return buffer.toByteArray();
	}
}

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
package jass.modern.core.apt;

import static jass.modern.core.compile.ContractJavaCompiler.CONTRACT_JAR;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.ContractJavaFile;
import jass.modern.core.compile.IExtendedCompilationTask;
import jass.modern.core.model.IAnnotationReference;
import jass.modern.core.model.IType;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Options;

/**
 * An JSR269 compliant annotation processor that works
 * as a wrapper for the {@link ContractJavaCompiler}.
 * <br /> <br />
 * Basically, this annotation processor works in three
 * distinct steps:
 * <ol>
 * <li>From the annotated elements a {@link IType model} is created.
 * <li>The model is passed on to the {@link ContractJavaCompiler}
 *  and the annotation values are getting checked.
 * <li>If errors or warnings have been issued during compilation
 *  they are forwared to the {@link ProcessingEnvironment}.
 * </ol>
 * This annotation processor understand two different options
 * <ul>
 * <li><code>verbose</code> = If set contract code is printed on System.out
 * <li><code>cp</code> = The classpath that is used for contract compilation
 * </ul> 
 * <br />
 * 
 * @author riejo
 */
@SupportedAnnotationTypes("jass.modern.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( { AnnotationProcessor.OPT_VERBOSE, AnnotationProcessor.OPT_CLASSPATH}) 
public class AnnotationProcessor extends AbstractProcessor {

	protected static final String OPT_VERBOSE = "verbose";
	protected static final String OPT_CLASSPATH = "cp";
	
	private final ContractJavaCompiler fCompiler = ContractJavaCompiler.getInstance();
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		try {
			setupClasspath();
			setupContractArchive();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		
		
		// (1) get all root for every annotated element
		Set<TypeElement> rootElements = getRootElements(annotations, roundEnv);
		if(rootElements.isEmpty())
			return false;
		
		// (2) create contract objects
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		IExtendedCompilationTask compilationTask = fCompiler.getCompilationTaskJava6(
				rootElements, diagnostics);
				
		// (3) print contract class to System.out
		printVerboseInfo(compilationTask);
		
		// (4) report compilation problems, iff any 
		boolean success = compilationTask.call();
		if(!success || !diagnostics.getDiagnostics().isEmpty()) {
			List<Diagnostic<? extends JavaFileObject>> diagnosis = diagnostics.getDiagnostics();
			
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosis) {
				
				if(diagnostic instanceof ContractDiagnostic) {
					ContractDiagnostic tmp = (ContractDiagnostic) diagnostic;
					printDiagnosticMessage(tmp.getAnnotationReference(), tmp);
					
				} else if (!(diagnostic.getSource() instanceof ContractJavaFile)) {
					printDiagnosticMessage(null, diagnostic);
					
				} else {
					ContractJavaFile source = (ContractJavaFile) diagnostic.getSource();
					long lineNumber = diagnostic.getLineNumber();
					
					IAnnotationReference value = source.getAnnotationReference(lineNumber);
					printDiagnosticMessage(value, diagnostic);
				}
			}
		}
		
		fCompiler.reset();
		return true;
	}

	/**
	 * Set the classpath of the internal compiler which is invoked
	 * during annotation processing. The classpath can (or in some
	 * cases must) be specified via the <code>-Acp</code> option 
	 * that is defined by this annotation processor or is taken f
	 * from the processing environment (works only with Sun JDK - 
	 * none API)
	 * @throws IOException 
	 */
	private void setupClasspath() throws IOException {
		
		String classpath = null;
		
		/*
		 * not using instanceof here because than in every case the JVM
		 * tries to load the JavacProcessingEnvironment-class file which, 
		 * for instance, is not possible with an IBM JVM.
		 */
		if (processingEnv.getClass().getName().equals(
				"com.sun.tools.javac.processing.JavacProcessingEnvironment")) {
			
			JavacProcessingEnvironment env = (JavacProcessingEnvironment) processingEnv;
			Options options = Options.instance(env.getContext());
			String classpath1 = options.get(OptionName.CP);
			String classpath2 = options.get(OptionName.CLASSPATH);
			classpath = classpath1 != null ? (classpath2 != null ? 
					classpath1 + File.pathSeparator + classpath2 : 
						classpath1) : 
							classpath2;
			
		} else {
			classpath = processingEnv.getOptions().get(OPT_CLASSPATH);
		}
		
		if(classpath != null && classpath.trim().length() != 0) {
			fCompiler.addClasspathEntry(classpath);
		}
	}
	 
	private void setupContractArchive() {
		Filer filer = processingEnv.getFiler();
		
		try {
			FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", CONTRACT_JAR);
			fCompiler.setContractArchiveOutputStream(fileObject.openOutputStream());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void printDiagnosticMessage(IAnnotationReference value, 
			Diagnostic<? extends JavaFileObject> diagnostic) {
		
		Messager messager = processingEnv.getMessager();
		if(value == null) {
			messager.printMessage(diagnostic.getKind(), diagnostic.getMessage(null));
			
		} else if(value instanceof IRevealableAnnotationValue) {

			IRevealableAnnotationValue revealableValue = (IRevealableAnnotationValue) value;
			messager.printMessage(diagnostic.getKind(), diagnostic.getMessage(null), 
					revealableValue.getElement(), revealableValue.getAnnotationMirror(), 
					revealableValue.getAnnotationValue());
		}
	}

	private void printVerboseInfo(IExtendedCompilationTask task) {

		boolean verbose = processingEnv.getOptions().containsKey(OPT_VERBOSE);
		if(!verbose)
			return;
		
		for (IType type : task.getTypes()) {
			try {
				ContractJavaFile file = new ContractJavaFile(type);
				System.out.println(file.getCharContent(true));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Set<TypeElement> getRootElements(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {

		HashSet<TypeElement> rootElements = new HashSet<TypeElement>();
		for (TypeElement annotation : annotations) {
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
			
			Iterator<? extends Element> iter = elements.iterator();
			if (iter.hasNext()) {
				rootElements.add(getRootElement(elements.iterator().next()));
			}
		}

		return rootElements;
	}

	private TypeElement getRootElement(Element element) {

		// (1) special case if some tries with an package
		if (element instanceof PackageElement) {
			return null;
		}

		// (2) check if element is innerclass, otherwise return
		if (element instanceof TypeElement) {

			TypeElement type = (TypeElement) element;
			if (type.getEnclosingElement() instanceof PackageElement) {
				return type;
			}
		}

		// (3) check enclosing element...
		return getRootElement(element.getEnclosingElement());
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element,
			AnnotationMirror annotation, ExecutableElement member,
			String userText) {
		
		
		return super.getCompletions(element, annotation, member, userText);
	}
	
	

}

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
package jass.modern.eclipse.apt.jsr175;

import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.ContractJavaFile;
import jass.modern.core.compile.IExtendedCompilationTask;
import jass.modern.core.model.IAnnotationReference;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.TypeFactoryComSunMirror.IComSunMirrorRevealable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.SourcePosition;

/**
 * An annotation processor factory which drives annotation processing
 * the apt-way (see apt and Java 5 for more details). This factory is
 * made available for the Eclipse JDT via the extension point
 * <code>org.eclipse.jdt.apt.core.annotationProcessorFactory</code>.
 * <br /><br />
 * Although an apt-based annotation processor works with every
 * Java 5 or later based JVM, this annotation processor is bundled
 * with the eclipse plugin. Aside from eclipse, the Java 6-based 
 * annotation processor shall be used. 
 * 
 * @author riejo
 */
public class ModernJassAnnotationProcessorFactory implements AnnotationProcessorFactory {

	private final class InternalAnnotationProcessor implements
			AnnotationProcessor {

		private static final String OPT_OUTPUT_LOC = "-d";

		static final String OPT_VERBOSE = "verbose";
		
		static final String OPT_CLASSPATH = "-classpath";

		private final EclipseAnnotationProcessorEnvironment fEnv;

		private InternalAnnotationProcessor(
				EclipseAnnotationProcessorEnvironment env) {
			
			fEnv = env;
		}

		public void process() {

			synchronized (_MUTEX) {
			
				// (2) configure compiler and enivronment
				boolean verbose = fEnv.getOptions().containsKey(OPT_VERBOSE);
				
				try {
					String output = fEnv.getOptions().get(OPT_OUTPUT_LOC);
					fCompiler.addClasspathEntry(output);
					String classpath = fEnv.getOptions().get(OPT_CLASSPATH);
					fCompiler.addClasspathEntry(classpath);
					fCompiler.setContractArchiveOutputStream(new FileOutputStream(
							output + File.separator + ContractJavaCompiler.CONTRACT_JAR));
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				// (3) compile contracts
				DiagnosticCollector<JavaFileObject> diagnostic = new DiagnosticCollector<JavaFileObject>();
				Collection<TypeDeclaration> types = fEnv.getTypeDeclarations();
				IExtendedCompilationTask task = fCompiler.getCompilationTaskJava5(types, diagnostic);
				
				if(verbose) {
					printVerbose(task);
				}
				
				boolean success = task.call();
				if (!success || !diagnostic.getDiagnostics().isEmpty()) {
					Messager messager = fEnv.getMessager();
					reportDiagnostics(messager, diagnostic,	null);
				}
			
				// (1) reset the compiler
				fCompiler.reset();
			}
		}

		protected void reportDiagnostics(Messager messager, DiagnosticCollector<JavaFileObject> diagnostic,
				Collection<Declaration> declarations) {
			
			List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnostic.getDiagnostics();
			
			for (Diagnostic<? extends JavaFileObject> diagnosis : diagnostics) {
		
				SourcePosition pos = null;
				IAnnotationReference reference = null;
				
				// (1) reference from contract diagnotic
				if (diagnosis instanceof ContractDiagnostic) {
					ContractDiagnostic contractDiagnosis = (ContractDiagnostic) diagnosis;
					reference = contractDiagnosis.getAnnotationReference();
		
				// (2) reference via ContractJavaFile
				} else if(diagnosis.getSource() instanceof ContractJavaFile){
					ContractJavaFile source = (ContractJavaFile) diagnosis.getSource();
					long lineNumber = diagnosis.getLineNumber();
					reference = source.getAnnotationReference(lineNumber);
				}
				
				// (3) validate message
				if(reference != null && reference instanceof IComSunMirrorRevealable) {
					pos = ((IComSunMirrorRevealable) reference).getSourcePosition();
				}
				
				String msg = diagnosis.getMessage(null);
				
				switch (diagnosis.getKind()) {
				case ERROR:
					messager.printError(pos, msg);
					break;
		
				case NOTE:
					messager.printNotice(pos, msg);
					break;
					
				case OTHER:
				case MANDATORY_WARNING:
				case WARNING:
					messager.printWarning(pos, msg);
					break;
					
				/*
				 * so no diagnosis kind gets lost
				 */
				default:
					messager.printError(pos, msg);	
				}
			}
		}

		protected void printVerbose(IExtendedCompilationTask task) {
			
			for (IType type : task.getTypes()) {
				ContractJavaFile file = new ContractJavaFile(type);
				
				try {
					System.out.println(file.getCharContent(true));
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Used to synchronize the contract compilation process.
	 */
	final Object _MUTEX = new Object();
	
	private final ContractJavaCompiler fCompiler;

	public ModernJassAnnotationProcessorFactory() {
		fCompiler = ContractJavaCompiler.getInstance();
		fCompiler.disableCaching();
	}
	
	public AnnotationProcessor getProcessorFor(
			final Set<AnnotationTypeDeclaration> annotations,
			final AnnotationProcessorEnvironment e) {

		final EclipseAnnotationProcessorEnvironment env = (EclipseAnnotationProcessorEnvironment) e;

		return new InternalAnnotationProcessor(env);
	}
	
	public Collection<String> supportedAnnotationTypes() {
		return Arrays.asList("jass.modern.*");
	}

	public Collection<String> supportedOptions() {
		return Arrays.asList(InternalAnnotationProcessor.OPT_VERBOSE);
	}
}

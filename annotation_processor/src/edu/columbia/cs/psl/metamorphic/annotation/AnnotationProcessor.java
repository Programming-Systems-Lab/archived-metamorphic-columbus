package edu.columbia.cs.psl.metamorphic.annotation;


import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.Diagnostic;
import edu.columbia.cs.psl.metamorphic.compiler.MetamorphicTestCompiler;

@SupportedAnnotationTypes("edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
//@SupportedOptions( { AnnotationProcessor.OPT_VERBOSE, AnnotationProcessor.OPT_CLASSPATH}) 
public class AnnotationProcessor extends AbstractProcessor {
//	protected static final String OPT_VERBOSE = "verbose";
//	protected static final String OPT_CLASSPATH = "cp";
//	private final MetamorphicTestCompiler fCompiler = MetamorphicTestCompiler.getInstance();
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
//		for(TypeElement e : annotations)
//		{
//			System.out.println(e);
//		}
//		for ( TypeElement typeElement: annotations ) {
//            for ( Element element : roundEnv.getElementsAnnotatedWith( typeElement ) ) {
//                this.processingEnv.getMessager().printMessage( Diagnostic.Kind.ERROR, element.toString(), element );
//            }
//        }
//		CompilationTask compilationTask = fCompiler.getCompilationTaskJava6(
//				null, null);
		
		return true;
	}

}

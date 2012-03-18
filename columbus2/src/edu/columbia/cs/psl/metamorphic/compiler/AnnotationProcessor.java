package edu.columbia.cs.psl.metamorphic.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;


@SupportedAnnotationTypes("edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {

	private TypeElement getEnclosingClass(Element e) {
		if (e instanceof TypeElement)
			if (((TypeElement) e).getEnclosingElement() instanceof PackageElement)
				return (TypeElement) e;
		return getEnclosingClass(e.getEnclosingElement());

	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Filer filer = processingEnv.getFiler();

		for (TypeElement typeElement : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
				if (!classFiles.containsKey(getEnclosingClass(element))) {
						MetamorphicClassFile ac = new MetamorphicClassFile();
						ac.setTypeElement(getEnclosingClass(element));
						ac.setMethods(new ArrayList<ExecutableElement>());
						try {
							ac.setFile(filer.createSourceFile(getEnclosingClass(element).getQualifiedName().toString()+"_tests"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						classFiles.put(ac.getTypeElement(), ac);

				}

				if (element.getKind().equals(ElementKind.METHOD)) {
					// this is a method
					MetamorphicClassFile ac = classFiles.get(getEnclosingClass(element));
					ac.getMethods().add(((ExecutableElement) element));
				}
			}
		}
		if (annotations.size() > 0)
		{
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			MetamorphicPropertyCompiler compiler = MetamorphicPropertyCompiler.getInstance(diagnostics,this.processingEnv);
			
			for(MetamorphicClassFile c : classFiles.values())
				if(!c.isDone())
					compiler.compileTestCode(c);
		}
		return true;
	}
	private HashMap<TypeElement, MetamorphicClassFile>	classFiles = new HashMap<TypeElement, MetamorphicClassFile>();;
}

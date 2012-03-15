package edu.columbia.cs.psl.metamorphic.compiler;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler.CompilationTask;

public class MetamorphicTestCompiler {
	
	private static MetamorphicTestCompiler instance;
	public static MetamorphicTestCompiler getInstance() {
		if(instance == null)
			instance = new MetamorphicTestCompiler();
		return instance;
	}
	public CompilationTask getCompilationTaskJava6(Collection<TypeElement> typesElements, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		return null;
	}
}

package edu.columbia.cs.psl.metamorphic.compiler;


import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner6;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

public class CompilerMethodVisitor extends ElementScanner6<Void, AnnotatedClass> {
	@Override
	public Void visitType(TypeElement e, AnnotatedClass p) {
		return super.visitType(e, p);
	}
	
	public Void visitExecutable(ExecutableElement e, AnnotatedClass parent) {
		if(e.getAnnotation(Metamorphic.class) != null)
			parent.getMethods().add(e);
		return super.visitExecutable(e, parent);
	}
}

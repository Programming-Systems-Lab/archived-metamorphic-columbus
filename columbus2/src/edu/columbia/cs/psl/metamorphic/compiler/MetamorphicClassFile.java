package edu.columbia.cs.psl.metamorphic.compiler;

import java.util.ArrayList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class MetamorphicClassFile {
	
	private TypeElement typeElement;
	private ArrayList<ExecutableElement> methods;
	private boolean done;
	private JavaFileObject ob;

	public boolean isDone() {
		return done;
	}
	public void markDone() {
		this.done = true;
	}
	public TypeElement getTypeElement() {
		return typeElement;
	}
	public ArrayList<ExecutableElement> getMethods() {
		return methods;
	}
	public void setTypeElement(TypeElement clazz) {
		this.typeElement = clazz;
	}
	public void setMethods(ArrayList<ExecutableElement> methods) {
		this.methods = methods;
	}
	public void setFile(JavaFileObject createSourceFile) {
		this.ob = createSourceFile;
	}
	public JavaFileObject getFile()
	{
		return this.ob;
	}
}

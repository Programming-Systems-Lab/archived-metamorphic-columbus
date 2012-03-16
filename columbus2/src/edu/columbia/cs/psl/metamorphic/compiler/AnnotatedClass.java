package edu.columbia.cs.psl.metamorphic.compiler;

import java.util.ArrayList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class AnnotatedClass {
	private TypeElement clazz;
	private ArrayList<ExecutableElement> methods;
	private JavaFileObject jfo;
	
	public JavaFileObject getJfo() {
		return jfo;
	}
	public void setJfo(JavaFileObject jfo) {
		this.jfo = jfo;
	}
	public TypeElement getClazz() {
		return clazz;
	}
	public ArrayList<ExecutableElement> getMethods() {
		return methods;
	}
	public void setClazz(TypeElement clazz) {
		this.clazz = clazz;
	}
	public void setMethods(ArrayList<ExecutableElement> methods) {
		this.methods = methods;
	}
}

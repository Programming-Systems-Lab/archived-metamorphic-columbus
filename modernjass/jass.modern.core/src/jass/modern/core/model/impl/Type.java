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
package jass.modern.core.model.impl;

import jass.modern.core.model.IElementVisitor;
import jass.modern.core.model.IType;
import jass.modern.core.util.Elements;

import java.util.LinkedList;
import java.util.List;

public class Type extends Element implements IType {

	public static final String JAVA_LANG_OBJECT = "java.lang.Object";
	
	private Kind fKind;
	protected LinkedList<String> fGenericSignature = new LinkedList<String>();
	protected LinkedList<String> fInterfaces = new LinkedList<String>();
	protected String fSuperClass;
	
	public Type(String name, Kind kind) {
		super(name.replace('/', '.').replace('$', '.'));
		
		if(!Elements.isTypeName(name.replace('/', '.'))) {
			throw new IllegalArgumentException("Illegal name: " + name);
		}
		
//		if(name.endsWith(".class") || name.endsWith(".java")) {
//			throw new IllegalArgumentException("Illegal name: " + name);
//		}
		
		if(kind == null) {
			throw new NullPointerException("kind must not be null");
		}
		
		fKind = kind;
	}

	@SuppressWarnings("unchecked")
	protected Type(Type other) {
		super(other);
		
		fGenericSignature = (LinkedList<String>) other.fGenericSignature.clone();
		fInterfaces = (LinkedList<String>) other.fInterfaces.clone();
		fSuperClass = other.fSuperClass;
		fKind = other.fKind;
	}
	
	public String getSuperclass() {
		return fSuperClass;
	}

	public void setSuperclass(String className) {
//		if(!Elements.isTypeName(className))
//			throw new IllegalArgumentException("Illegal class name " + className);
		
		fSuperClass = className;
	}

	public List<String> getInterfaces() {
		return fInterfaces;
	}

	public void addInterface(String className) {
//		if(!Elements.isTypeName(className))
//			throw new IllegalArgumentException("Illegal class name " + className);
		
		fInterfaces.add(className);
	}

	public void removeInterface(String className) {
		fInterfaces.remove(className);
	}

	public List<String> getGenericSignature() {
		return fGenericSignature;
	}

	public void addGenericSignature(String type) {
		fGenericSignature.add(type);
	}
	
	public void removeGenericSignature(String type) {
		fGenericSignature.remove(type);
	}
	
	public String getQualifiedName() {
		return super.getSimpleName();
	}
	
	@Override
	public String getSimpleName() {
		String fqn = super.getSimpleName();
		int index = fqn.lastIndexOf('.');
		return index != -1 ? fqn.substring(index + 1) : fqn;
	}

	@Override
	public <P> void accept(IElementVisitor<P> visitor, P param) {
	
		visitor.visit((IType) this, param);
	}
	
	public Object clone() {
		return new Type(this);
	}

	@Override
	public Kind getKind() {
		return fKind;
	}
}

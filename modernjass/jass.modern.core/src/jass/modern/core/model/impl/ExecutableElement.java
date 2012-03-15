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

import jass.modern.core.model.IElement;
import jass.modern.core.model.IElementVisitor;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IVariable;
import jass.modern.core.util.Elements;

import java.util.LinkedList;
import java.util.List;

public class ExecutableElement extends Element implements IExecutable {

	protected LinkedList<String> fGenericSignature = new LinkedList<String>();
	protected LinkedList<String> fExceptions = new LinkedList<String>();
	protected boolean fConstructor = false;
	protected String fReturnType;
	
	public ExecutableElement(String name, String returnType) {
		super(name);
		fReturnType = returnType;
	}

	@SuppressWarnings("unchecked")
	protected ExecutableElement(ExecutableElement other) {
		super(other);
		fGenericSignature = (LinkedList<String>) other.fGenericSignature.clone();
		fExceptions = (LinkedList<String>) other.fExceptions.clone();
		fConstructor = other.fConstructor;
		fReturnType = other.fReturnType;
	}

	public List<String> getExceptions() {
		return fExceptions;
	}

	public void addException(String execption) {
		fExceptions.add(execption);
	}

	public void removeException(String exec) {
		fExceptions.remove(exec);
	}

	public List<IVariable> getParameters() {
		List<IVariable> parameters = new LinkedList<IVariable>();
		for (IElement element : Elements.filter(IVariable.class, getEnclosedElements())) {
			parameters.add((IVariable) element);
		}
		
		return parameters;
	}

	public void addParameter(IVariable var) {
		addEnclosedElement(var);
	}

	public void removeParameter(IVariable var) {
		removeEnclosedElement(var);
	}

	public List<String> getGenericSignature() {
		return fGenericSignature;
	}

	public void addGenericSignature(String type) {
		fGenericSignature.add(type);
	}
	
	public String getReturnType() {
		return fReturnType;
	}

	public void setReturnType(String type) {
		fReturnType = type;
	}

	public boolean isConstructor() {
		return fConstructor;
	}
	
	public void setConsutructor(boolean constructor) {
		fConstructor = constructor;
	}

	@Override
	public <P> void accept(IElementVisitor<P> visitor, P param) {
		
		visitor.visit((IExecutable) this, param);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if (!(obj instanceof IExecutable))
			return false;
		
		IExecutable other = (IExecutable) obj;
		return internalSignature(this).equals(internalSignature(other));
	}
	
	@Override
	public int hashCode() {
		return internalSignature(this).hashCode();
	}
	
	private String internalSignature(IExecutable element) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(element.isConstructor());
		buffer.append(element.getReturnType());
		buffer.append(element.getSimpleName());
		for (IVariable var : element.getParameters()) {
			buffer.append(var.getType());
		}
		
		for(String str : element.getExceptions()) {
			buffer.append(str);
		}
		
		return buffer.toString();
	}
	
	public Object clone() {
		return new ExecutableElement(this);
	}
}

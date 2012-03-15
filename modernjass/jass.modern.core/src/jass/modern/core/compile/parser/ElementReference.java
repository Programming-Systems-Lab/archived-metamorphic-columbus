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
package jass.modern.core.compile.parser;

import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;

public class ElementReference implements IElementReference {

	private String fName;
	private int fParameterCount;
	private Type fType;
	private IElementReference fEnclosingElement;
	private IElementReference fEnclosedElement;
	
	public ElementReference(String name, Type type) {
		this(name, type, 0);
	}
	
	public ElementReference(String name, Type type, int parameterCount) {
		fName = name;
		fType = type;
		fParameterCount = parameterCount;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public int getParameterCount() {
		return fParameterCount;
	}

	@Override
	public Type getType() {
		return fType;
	}
	
	@Override
	public boolean matches(IElement element) {
		if(!element.getSimpleName().equals(fName))
			return false;
		
		if (element instanceof IExecutable) {
			int tmp = ((IExecutable) element).getParameters().size();
			
			if(getType() != Type.METHOD_INVOCATION)
				return false;
			
			if(tmp != fParameterCount)
				return false;
		}
		
		return true;
	}

	@Override
	public void setEnclosedElement(IElementReference reference) {
		fEnclosedElement = reference;
	}

	@Override
	public IElementReference getEnclosedElement() {
		return fEnclosedElement;
	}

	@Override
	public IElementReference getEnclosingElement() {
		return fEnclosingElement;
	}

	@Override
	public void setEnclosingElement(IElementReference reference) {
		fEnclosingElement = reference;
	}
	
	public String toString() {
		return "[" + getType() + "] " + getName() + (
				getType() == Type.METHOD_INVOCATION ? "("+getParameterCount() + ")" : "");
	}

}

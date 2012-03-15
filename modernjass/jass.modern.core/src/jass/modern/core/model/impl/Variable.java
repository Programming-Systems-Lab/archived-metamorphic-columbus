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
import jass.modern.core.model.IVariable;

import java.util.LinkedList;
import java.util.List;

public class Variable extends Element implements IVariable {

	protected String fType;
	protected LinkedList<String> fGenericSignature = new LinkedList<String>();
	
	public Variable(String name, String type) {
		super(name);
		fType = type.replace('/', '.');
	}

	@SuppressWarnings("unchecked")
	protected Variable(Variable other) {
		super(other);
		fType = other.fType;
		fGenericSignature = (LinkedList<String>) other.fGenericSignature.clone();
	}
	
	public String getType() {
		return fType;
	}
	
	@Override
	public List<String> getGenericSignature() {
		return fGenericSignature;
	}

	public void addGenericSignature(String type) {
		fGenericSignature.add(type);
	}
	
	public String toString() {
		return getType() + " " + getSimpleName();
	}
	
	@Override
	public <P> void accept(IElementVisitor<P> visitor, P param) {
		visitor.visit((IVariable) this, param);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IVariable))
			return false;
		
		IVariable tmp = (IVariable) o;
		
		return getType().equals(tmp.getType()) && super.equals(tmp); 
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() * getType().hashCode();
	}
}

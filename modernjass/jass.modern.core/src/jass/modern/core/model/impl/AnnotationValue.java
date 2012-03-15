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
/**
 * 
 */
package jass.modern.core.model.impl;

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AnnotationValue extends Element implements IAnnotationValue {

	LinkedList<Object> fValues = new LinkedList<Object>();
		
	public AnnotationValue(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	protected AnnotationValue(AnnotationValue other) {
		super(other);
		fValues = (LinkedList<Object>) other.fValues.clone();
	}
	
	public AnnotationValue(IAnnotation parent, String name, Object... value) {
		super(name);
		parent.addEnclosedElement(this);
		
		if(value != null)
			fValues.addAll(Arrays.asList(value));
	}

	public List<Object> getValue() {
		return fValues;
	}

	public void setValue(Object... values) {
		fValues.clear();
		fValues.addAll(Arrays.asList(values));
		
		for (Object object : values) {
			if (object instanceof IElement) {
				super.addEnclosedElement((IElement) object);
			}
		}
	}

	@Override
	public void addEnclosedElement(IElement element) {
		element.setEnclosingElement(this);
		fValues.add(element);
	}

	@Override
	public List<IElement> getEnclosedElements() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj))
			return false;
		
		if (!(obj instanceof IAnnotationValue))
			return false;
		
		return ((IAnnotationValue) obj).getValue().equals(getValue());
	}
	
	public Object clone() {
		return new AnnotationValue(this);
	}
}

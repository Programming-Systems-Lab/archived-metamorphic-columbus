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
package jass.modern.core.apt;

import jass.modern.core.model.impl.AnnotationValue;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class RevealableAnnotationValue extends AnnotationValue implements
		IRevealableAnnotationValue {

	private Element fElement;
	private AnnotationMirror fAnnotationMirror;
	private javax.lang.model.element.AnnotationValue fAnnotationValue;
	
	public RevealableAnnotationValue(String name, Element element, 
			AnnotationMirror annotationMirror, javax.lang.model.element.AnnotationValue annotationValue) {
		super(name);
		
		fElement = element;
		fAnnotationMirror = annotationMirror;
		fAnnotationValue = annotationValue;
	}

	public RevealableAnnotationValue(RevealableAnnotationValue other) {
		super(other);
		fElement = other.fElement;
		fAnnotationMirror = other.fAnnotationMirror;
		fAnnotationValue = other.fAnnotationValue;
	}
	
	public AnnotationMirror getAnnotationMirror() {
		return fAnnotationMirror;
	}

	public javax.lang.model.element.AnnotationValue getAnnotationValue() {
		return fAnnotationValue;
	}

	public Element getElement() {
		return fElement;
	}
	
	public Object clone() {
		return new RevealableAnnotationValue(this);
	}
}

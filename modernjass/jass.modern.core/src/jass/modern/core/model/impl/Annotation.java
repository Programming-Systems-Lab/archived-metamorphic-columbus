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

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElementVisitor;
import jass.modern.core.util.Elements;

public class Annotation extends Element implements IAnnotation {
	
	public Annotation(String name) {
		super(name);
	}
	
	public Annotation(Annotation other) {
		super(other);
	}

	public IAnnotationValue getValue(String name) {

		return Elements.filterFirst(name, IAnnotationValue.class, 
				getEnclosedElements());
	}

	/**
	 * Shorthand for <code>getValue("value")</code>.
	 * @see #getValue(String)
	 */
	public IAnnotationValue getDefaultValue() {
		return getValue("value");
	}

	@Override
	public <P> void accept(IElementVisitor<P> visitor, P param) {
		visitor.visit((IAnnotation) this, param);
	}
	
}

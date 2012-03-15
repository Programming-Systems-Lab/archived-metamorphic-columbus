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
package jass.modern.core.util;

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IContractVariable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;

import java.util.List;

public class ElementScanner<P> extends EmptyElementVisitor<P> {

	private Class<? extends IElement> fFilterType;

	public ElementScanner() {
		this(null);
	}
	
	public ElementScanner(Class<? extends IElement> filterType) {
		fFilterType = filterType;
	}

	protected List<? extends IElement> filter(IElement element){
		if(fFilterType == null)
			return element.getEnclosedElements();
		
		return Elements.filter(fFilterType, element.getEnclosedElements());
	}

	@Override
	public void visit(IType element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IContractVariable element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IVariable element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IContractExecutable element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IExecutable element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IAnnotation element, P param) {
		scan(filter(element), param);
	}

	@Override
	public void visit(IAnnotationValue element, P param) {
		List<Object> value = element.getValue();
		for (Object object : value) {
			if (object instanceof IElement) {
				visit( (IElement) object, param);
			}
		}
	}

	@Override
	public void visitUnkown(IElement element, P param) {

	}
}

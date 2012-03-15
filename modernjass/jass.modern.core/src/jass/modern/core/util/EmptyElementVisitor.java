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
import jass.modern.core.model.IElementVisitor;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;

import java.util.List;

public class EmptyElementVisitor<P> implements IElementVisitor<P> {

	public void scan(List<? extends IElement> elements, P param) {
		
		IElement[] tmp = elements.toArray(new IElement[elements.size()]);
		for (IElement element : tmp) {
			visit(element, param);
		}
	}
	
	public void visit(IElement element, P param) {
		if (element instanceof IContractVariable) {
			IContractVariable tmp = (IContractVariable) element;
			visit(tmp, param);
			
		} else if (element instanceof IVariable) {
			IVariable tmp = (IVariable) element;
			visit(tmp, param);
			
		} else if (element instanceof IContractExecutable) {
			IContractExecutable tmp = (IContractExecutable) element;
			visit(tmp, param);
			
		}else if (element instanceof IExecutable) {
			IExecutable tmp = (IExecutable) element;
			visit(tmp, param);
			
		} else if (element instanceof IContractExecutable) {
			IContractExecutable tmp = (IContractExecutable) element;
			visit(tmp, param);
			
		} else if (element instanceof IAnnotation) {
			IAnnotation tmp = (IAnnotation) element;
			visit(tmp, param);
			
		} else if (element instanceof IType) {
			IType tmp = (IType) element;
			visit(tmp, param);
			
		} else if(element instanceof IAnnotationValue) {
			IAnnotationValue tmp = (IAnnotationValue) element;
			visit(tmp, param);
			
		} else {
			visitUnkown(element, param);
		}
	}

	public void visitUnkown(IElement element, P param) {
	}
	
	public void visit(IVariable element, P param) {
	}

	public void visit(IContractVariable element, P param) {
	}

	public void visit(IExecutable element, P param) {
	}

	public void visit(IContractExecutable element, P param) {
	}

	public void visit(IAnnotation element, P param) {
	}

	public void visit(IAnnotationValue element, P param) {
	}

	public void visit(IType element, P param) {
	}
}

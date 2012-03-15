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
package jass.modern.core.model;

public interface IElementVisitor<P> {
	
	void visit(IElement element, P param);
	
	void visit(IVariable element, P param);
	
	void visit(IContractVariable element, P param);
	
	void visit(IExecutable element, P param);
	
	void visit(IContractExecutable element, P param);
	
	void visit(IAnnotation element, P param);
	
	void visit(IAnnotationValue element, P param);
	
	void visit(IType element, P param);
	
}

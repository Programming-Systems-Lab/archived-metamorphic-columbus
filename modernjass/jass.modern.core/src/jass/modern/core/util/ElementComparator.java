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
package jass.modern.core.util;

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractElement;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;

import java.util.Comparator;

public class ElementComparator implements Comparator<IElement> {

	public int compare(IElement o1, IElement o2) {
		return ordinal(o1) - ordinal(o2);
	}
	
	private int ordinal(IElement element) {
		if (element instanceof IVariable) 
			return 1;
		
		if (element instanceof IContractElement) 
			return 2;
		
		if(element instanceof IExecutable) 
			return 3;
		
		if (element instanceof IContractExecutable) 
			return 4;
		
		if (element instanceof IAnnotation)
			return 5;
		
		if (element instanceof IAnnotationValue)
			return 6;
		
		if(element instanceof IType)
			return 7;
		
		return 0;
	}
}

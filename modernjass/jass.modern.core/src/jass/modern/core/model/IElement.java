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

import java.util.List;
import java.util.Set;

public interface IElement extends Cloneable {
	
	IElement getEnclosingElement();
	
	void setEnclosingElement(IElement element);
	
	List<IElement> getEnclosedElements();
	
	void addEnclosedElement(int index, IElement element);
	
	void addEnclosedElement(IElement element);
	
	void removeEnclosedElement(IElement element);
	
	Set<Modifier> getModifiers();
	
	void addModifier(Modifier modifier);
	
	void removeModifier(Modifier modifier);
	
	String getSimpleName();
	
	void setSimpleName(String name);
	
	<P> void accept(IElementVisitor<P> visitor, P param);
	
	Object clone();
}

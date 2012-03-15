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

import jass.modern.core.model.IElement;
import jass.modern.core.model.IElementVisitor;
import jass.modern.core.model.Modifier;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Element implements IElement {

	protected String fSimpleName;
	protected IElement fEnclosingElement;
	private LinkedList<IElement> fEnclosedElements = new LinkedList<IElement>();
	protected HashSet<Modifier> fModifier = new HashSet<Modifier>();
	
	public Element(String name){
		fSimpleName = name;
	}
	
	@SuppressWarnings("unchecked")
	protected Element(Element other) {
		fSimpleName = other.fSimpleName;
		fEnclosingElement = other.fEnclosingElement;
		fEnclosedElements = (LinkedList<IElement>) other.fEnclosedElements.clone();
		fModifier = (HashSet<Modifier>) other.fModifier.clone();
	}
	
	public List<IElement> getEnclosedElements() {
		return fEnclosedElements;
	}
	
	public void addEnclosedElement(int index, IElement element) {
		if(fEnclosedElements.contains(element)) {
			return;
		}
		
		fEnclosedElements.add(index, element);
		element.setEnclosingElement(this);
	}
	
	public void addEnclosedElement(IElement element) {
		addEnclosedElement(fEnclosedElements.size(), element);
	}

	public void removeEnclosedElement(IElement element) {
		boolean removed = fEnclosedElements.remove(element);
		if(removed)
			element.setEnclosingElement(null);
	}

	public IElement getEnclosingElement() {
		return fEnclosingElement;
	}
	
	public void setEnclosingElement(IElement element) {
		fEnclosingElement = element;
	}

	public Set<Modifier> getModifiers() {
		return fModifier;
	}
	
	/**
	 * This method ensure that only one
	 * visibility modifier exists. 
	 */
	public void addModifier(Modifier modifier) {
		switch (modifier) {
		case PRIVATE:
		case PACKAGE_PRIVATE:
		case PROTECTED:
		case PUBLIC:
			/*
			 * drop through & remove all 
			 * visibility modifiers
			 */
			fModifier.remove(Modifier.PRIVATE);
			fModifier.remove(Modifier.PACKAGE_PRIVATE);
			fModifier.remove(Modifier.PROTECTED);
			fModifier.remove(Modifier.PUBLIC);
			break;
		}
		fModifier.add(modifier);
	}

	public void removeModifier(Modifier modifier) {
		fModifier.remove(modifier);
	}

	public String getSimpleName() {
		return fSimpleName;
	}

	public void setSimpleName(String name) {
		fSimpleName = name;
	}

	public <P> void accept(IElementVisitor<P> visitor, P param) {
		visitor.visit(this, param);
	}
	
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Element))
			return false;
		
		Element other = (Element) obj;
		return fSimpleName.equals(other.fSimpleName) && fEnclosedElements.equals(other.fEnclosedElements);
	}
	
	public int hashCode() {
		return 13 * fSimpleName.hashCode();
	}
	
	/**
	 * see {@link #Element(Element)}
	 */
	public Object clone() {
		return new Element(this);
	}
	
	public String toString() {
		return getSimpleName() +" << "+ getEnclosingElement();
	}
}

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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * An enum type to represent all kinds of java modifiers. In general, 
 * modifiers can be applied to types, fields, and methods. 
 * <br />
 * 
 * @author riejo
 */
public enum Modifier {
	
	/** 
	 * The public modifier - <code>0x0001</code>
	 */
	PUBLIC( 0x0001),
	
	/**
	 * The protected modifier - <code>0x0004</code>
	 */
	PROTECTED( 0x0004),
	
	/**
	 * The package private modifier - <code>0x0000</code>.
	 * <br />
	 * Actually, this modifier is only a placeholder
	 * for the empty modifier.
	 */
	PACKAGE_PRIVATE( 0x0000),
	
	/**
	 * The private modifier - <code>0x0002</code>
	 */
	PRIVATE( 0x0002),
	
	/**
	 * The modifier static - <code>0x0008</code>
	 */
	STATIC( 0x0008),
	
	/**
	 * The final modifier - <code>0x0010</code>
	 */
	FINAL( 0x0010),
	
	/**
	 * The volatile modifier - <code>0x0040</code>.
	 * <em>Works for fields only.</em>
	 */
	VOLATILE( 0x0040),
	
	/**
	 * The transient modifier  - <code>0x0080</code>.
	 * <em>Works for fields only.</em>
	 */
	TRANSIENT( 0x0080),
	
	/**
	 * The abstract modifier - <code>0x0400</code>
	 */
	ABSTRACT( 0x0400),
	
	/**
	 * The synchronized modifier - <code>0x0020</code>
	 */
	SYNCHRONIZED( 0x0020),
	
	/**
	 * The native modifier - <code>0x0100</code>
	 */
	NATIVE( 0x0100);
	
	
	private static final Set<Modifier> VISIBILITY = EnumSet.of(
			PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE);
	
	int flag;
	
	Modifier(int n) {
		flag = n;
	}
	
	/**
	 * Returns the string representation of this 
	 * modifier. 
	 * @return The lower-cased name of this modifier.
	 * 	Returns the empty string iff 
	 * 	<code>this == PACKAGE_PRIVATE</code>.
	 */
	public String toString() {
		switch (this) {
		case PACKAGE_PRIVATE:	return "";
		default:	return name().toLowerCase();
		}
	}
	
	/**
	 * 
	 * @param modifiers A (possibly empty) list of modifiers
	 * @return Composes a flag by connecting all passed modifiers
	 * 	with the locigal-OR. Default is <code>0</code>.
	 */
	public static int flag(Modifier... modifiers) {
		int tmp = 0;
		for (Modifier modifier : modifiers) {
			tmp |= modifier.flag;
		}
		
		return tmp;
	}
	
	/**
	 * Converts a bitset into the accordant set of
	 * modifiers.<br />
	 * <em>Note:</em> The {@link #PACKAGE_PRIVATE}
	 * modifier is added to the returned set, if
	 * no other visibility modifier was found.
	 * 
	 * @param bitset
	 * @return
	 */
	public static Set<Modifier> flag(int bitset) {
		Set<Modifier> set = new HashSet<Modifier>();
		boolean hasVisibility = false;
		
		for (Modifier modifier : values()) {
			if( (bitset & modifier.flag) != 0) {
				set.add(modifier);
				hasVisibility |= VISIBILITY.contains(modifier); 
			}
		}
		
		/*
		 * Add the package private modifier
		 * if no visiblity was defined.
		 */
		if(!hasVisibility) {
			set.add(PACKAGE_PRIVATE);
		}
		
		return set;
	}
	
	/**
	 * Converts a collection of {@link javax.lang.model.element.Modifier}
	 * into a set of {@link Modifier}s.
	 * @param modifiers
	 * @return
	 */
	public static Set<Modifier> convert(Collection<javax.lang.model.element.Modifier> modifiers){
		Set<Modifier> set = new HashSet<Modifier>();
		
		if(! (modifiers.contains(javax.lang.model.element.Modifier.PUBLIC) ||
			modifiers.contains(javax.lang.model.element.Modifier.PROTECTED) ||
			modifiers.contains(javax.lang.model.element.Modifier.PRIVATE))) {
		
			set.add(Modifier.PACKAGE_PRIVATE);
		}
		
		for (javax.lang.model.element.Modifier modifier : modifiers) {
			switch (modifier) {
			case PUBLIC:
				set.add(Modifier.PUBLIC);
				break;
			case PROTECTED:
				set.add(Modifier.PROTECTED);
				break;
			case PRIVATE:
				set.add(Modifier.PRIVATE);
				break;
			case STATIC:
				set.add(Modifier.STATIC);
				break;
			case FINAL:
				set.add(Modifier.FINAL);
				break;
			case VOLATILE:
				set.add(Modifier.VOLATILE);
				break;
			case TRANSIENT:
				set.add(Modifier.TRANSIENT);
				break;
			case ABSTRACT:
				set.add(Modifier.ABSTRACT);
				break;
			case SYNCHRONIZED:
				set.add(Modifier.SYNCHRONIZED);
				break;
			case NATIVE:
				set.add(Modifier.NATIVE);
				break;
			}
		}
		return set;
	}
}

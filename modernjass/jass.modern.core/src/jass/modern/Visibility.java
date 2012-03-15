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
package jass.modern;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import jass.modern.core.model.Modifier;

/**
 * An enumeration type which is to define
 * the visibility of contract annotations.
 * <br />
 * In general, the visibility is analog to
 * the modifiers defined in chapter 6.6 of
 * the Java language specification. However, 
 * in addition to <code>public, protected,
 * package private, </code>and <code>private</code>
 * a type {@link #TARGET} exists which is
 * used to express that a specification
 * inherits the visibility of its target.
 * 
 * @see Invariant#visibility()
 * @see SpecCase#visibility()
 * @author riejo
 */
public enum Visibility {
	
	/**
	 * The <code>public</code> modifier.
	 */
	PUBLIC,
	
	/**
	 * The <code>protected</code> modifier.
	 */
	PROTECTED,
	
	/**
	 * The <code>package private</code> modifier.
	 */
	PACKAGE_PRIVATE,
	
	/**
	 * The <code>private</code> modifier.
	 */
	PRIVATE,
	
	/**
	 * Inherits the visibility of its
	 * target.
	 */
	TARGET;
	
	/**
	 * Returns the visibility which is encoded in the
	 * accessflag.
	 * See {@link http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#1513}
	 * for values of accessflag.
	 * 
	 * @see #mask(int, int)
	 * @param accessflag
	 * @return Default is {@value #PACKAGE_PRIVATE}, {@link #TARGET} 
	 * 	will never be returned, otherwise it is depended on the 
	 * 	accessflag. 
	 */
	public static Visibility parseVisibility(int accessflag) {
		
		if(mask(accessflag, ACC_PUBLIC))
			return PUBLIC;
		
		if(mask(accessflag, ACC_PROTECTED))
			return PROTECTED;
		
		if(mask(accessflag, ACC_PRIVATE))
			return PRIVATE;
		
		return PACKAGE_PRIVATE;
	}
	
	private static boolean mask(int mask, int flag) {
		return (mask & flag) != 0;
	}
	
	public static Visibility parseVisibility(Modifier modifier) {
		switch (modifier) {
			case PUBLIC:			return PUBLIC;
			case PROTECTED:			return PROTECTED;
			case PACKAGE_PRIVATE:	return PACKAGE_PRIVATE;
			case PRIVATE:			return PRIVATE;
			default:	return null;
		}
	}
	
	public Modifier toModifier() {
		switch (this) {
			case PUBLIC:			return Modifier.PUBLIC;
			case PROTECTED:			return Modifier.PROTECTED;
			case PACKAGE_PRIVATE:	return Modifier.PACKAGE_PRIVATE;
			case PRIVATE:			return Modifier.PRIVATE;
			default: 	return null;
		}
	}
	
	public String toString() {
		return name().toLowerCase();
	}
}

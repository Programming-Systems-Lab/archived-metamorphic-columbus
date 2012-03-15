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
package jass.modern.core.bytecode.contracts.handle;

/**
 * Defines the places where a method contract can 
 * be placed at. This is 
 * <ol>
 * <li>at the beginning of a method <em>BEFORE</em>
 * <li>at the end of a method reached by a reaching 
 * 	a return statement - <em>AFTER</em>
 * <li>at the end of a method reached by an 
 * 	execption - <em>FINALLY</em>
 * </ol>
 * 
 * @author riejo
 */
public enum ContractTarget {
	
	PRE, POST, SIGNALS_POST;
	
	/**
	 * Parses the strings <code>pre, post, </code> and <code>signals</code>
	 * and returns the accordant {@link ContractTarget}. This method is
	 * case<em>in</em>sensitive.
	 * 
	 * @param str <code>pre, post, </code> or <code>signals</code>
	 * @throws IllegalArgumentException if <code>str</code> does not 
	 * 	match any of the expected strings.
	 * @return The accordant {@link ContractTarget}.
	 */
	public static ContractTarget parseTarget(String str) {
		
		if(str.equalsIgnoreCase("pre"))
			return PRE;
		if(str.equalsIgnoreCase("post"))
			return POST;
		if(str.equalsIgnoreCase("signals"))
			return SIGNALS_POST;
		
		return null;
	}
	
	
	public String toString() {
		switch(this) {
		case PRE: 	return "pre";
		case POST:		return "post";
		case SIGNALS_POST:	return "signals";
		
		default: throw new RuntimeException();
		}
	}
}

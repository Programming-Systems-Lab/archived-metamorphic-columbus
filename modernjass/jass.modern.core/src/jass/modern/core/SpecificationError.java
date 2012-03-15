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
package jass.modern.core;

/**
 * A SpecificationError is thrown when a contract could 
 * not be evaluated. For instance, when <code>o</code>
 * is <code>null</code> the following specification will
 * fail with an {@link SpecificationError}. 
 * <pre>
 * &#064;Pre("o.hashCode() < 4")
 * void m(Object o){ 
 *  //..
 * }
 * </pre>
 * <b>Note</b> that a specification error always
 * has a {@link #getCause() cause} that is the 
 * root of this exception. Thus, it is useful
 * to call {@link #getCause()} to debug the
 * specification.
 * 
 * 
 * @author riejo
 */
public class SpecificationError extends Error {

	/**
	 * 
	 * @param cause The cause of this specification
	 * 	error.
	 */
	public SpecificationError(Exception cause) {
		initCause(cause);
	}
}

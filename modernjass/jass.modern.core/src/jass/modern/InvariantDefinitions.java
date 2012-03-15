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

import jass.modern.meta.Container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A {@link Container container}-annotation for {@link Invariant}iants. 
 * <br />
 * This annotation can only be added to type declarations. E.g:
 * <pre>
 * &#064;Invariants( 
 * 	{ &#064;Invar("fType != null"), &#064;Invar("fSize >= 0") })
 * class CU {
 *   Object fType = new Object();
 *   int fSize = 123;
 * 
 *   //...
 * }
 * </pre>
 * Note, that the {@link Invariant}-annotation can be placed at types 
 * and fields.
 * 
 * @author riejo
 */
@Documented
@Container
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface InvariantDefinitions {

	/**
	 * 
	 * @return The invariants captured in this container.
	 */
	Invariant[] value();
}

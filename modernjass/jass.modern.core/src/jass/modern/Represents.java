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

import jass.modern.meta.Code;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * The represents clause is used to bind a value to a
 * model variable.
 * 
 * @see Model
 * @author riejo
 */
@Documented
@Target( { ElementType.TYPE, ElementType.FIELD })
public @interface Represents {
	
	/**
	 * 
	 * @return The name of the model 
	 * 	variable. Must have been defined
	 * 	using {@link Def#name()}.
	 */
	String name();
	
	/**
	 * 
	 * @return The actual value of this
	 * 	model variable. It must be valid
	 * 	Java code which evaluates to the
	 * 	type defined via {@link Def#type()}.
	 */
	@Code String by();
}

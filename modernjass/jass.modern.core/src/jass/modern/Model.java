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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The definition of the model variable
 * which consists of a {@link #name()} and
 * a {@link #type()}.
 * 
 * @author riejo
 */
@Documented
@Target({ ElementType.TYPE })
public @interface Model {

	/**
	 * @return Returns the name of the 
	 * 	model variable.
	 */
	String name();
	
	/**
	 * 
	 * @return Returns the type of the 
	 * 	model variable.
	 */
	Class<?> type();
}

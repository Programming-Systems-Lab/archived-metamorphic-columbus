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

import jass.modern.core.compile.desugar.Level2Desugarable;
import jass.modern.core.util.Primitive;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * A parameter annotation which is used to 
 * specify the length/size of an <em>array</em>, the size of any subtype of 
 * the superinterface <em>{@link Collection}</em>, and for the length
 * of <em>Strings</em>.
 * <br />
 * <pre>public void m (&#064;Length(12) int[] monkeys){    }</pre>
 *  is equivalent to
 * <pre>
 * &#064;Spec( &#064;Case( pre="monkeys.length == 12"))
 * public void m (int[] monkeys){    }
 * </pre>
 * even better
 * <pre>void m(&#064;Length(3) String p){    }</pre>
 * which is the same as
 * <pre>
 * &#064;Spec( &#064;Case( pre="p.length() == 3"))
 * void m(String p){	}
 * </pre>
 * @author riejo
 */
@Documented
@Target( value = { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD } )
@Level2Desugarable(
		pattern = "@Target.@Length == @ValueOf(value)", 
		types = {Primitive.Array.class, String.class, Collection.class})
public @interface Length {
	
	/**
	 * The length of the array or collection. Addresses 
	 * the 1st dimension always.
	 * @return
	 */
	long value();
}

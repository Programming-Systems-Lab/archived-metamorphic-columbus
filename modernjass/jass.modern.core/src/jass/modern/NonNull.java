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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation can be used to express that
 * a parameter or a field is not allowed to be <code>null</code>.
 * <br />
 * <br />
 * <em>Note on primitive types:</em> Since version 5, Java
 * enables {@link http://java.sun.com/j2se/1.5.0/docs/guide/language/autoboxing.html 
 * auto-boxing} so that the &#64;NotNull-annotation totally makes sence
 * for primitive types. 
 * <br />
 * For instance, the &#64;NotNull-annotation used in the code below
 * <pre>
 * void m(&#64;NotNull int a){	
 *  //.. 
 * }
 * </pre>
 * will save you, if one calls
 * <pre>
 * m( (Integer) null);
 * </pre>
 * 
 * @author riejo
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Level2Desugarable( 
	pattern = "(java.lang.Object) @Target != null", 
	types = { Object.class } )
public @interface NonNull {
	
	/*
	 * this is a marker annotation
	 */
}

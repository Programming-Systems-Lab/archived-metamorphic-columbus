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

import jass.modern.core.compile.transform.ModelVariableTransformer;
import jass.modern.core.compile.transform.QuantifierTransformer;
import jass.modern.meta.Code;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class invariant assertion. It can be placed
 * at type members (classes, interfaces, ...) and
 * fields.<br />
 * 
 * @author riejo
 */
@Documented
@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface Invariant {
	
	/**
	 * The value of this invariant. It must be
	 * valid Java code which evaluates to a 
	 * boolean. 
	 * 
	 * @return The expression of this invariant.
	 */
	@Code(translator = { QuantifierTransformer.class, ModelVariableTransformer.class }) 
	String value();
	
	/**
	 * A message which is printed in case this
	 * invariant is violated. The default value
	 * is the empty string.
	 * 
	 * @see AssertionError#getMessage()
	 * @return The violation message of this
	 * 	invariant.
	 */
	String msg() default "";
	
	/**
	 * The {@link Visibility} of this invariant.
	 * Note, that for instance the visibility of
	 * a private field may not be public.
	 * 
	 * @return The visibiliy of this invariant which
	 * 	is not allowd to be greater (<em>'more public'</em>)
	 * 	then its target.
	 */
	Visibility visibility() default Visibility.TARGET;
	
	/**
	 * An invariant can be used in static and instance 
	 * contexts or in instance only contexts.
	 * 
	 * @return
	 */
	Context context() default Context.INSTANCE;
}

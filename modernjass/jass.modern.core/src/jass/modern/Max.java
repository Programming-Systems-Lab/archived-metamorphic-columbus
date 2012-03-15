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
 * Defines an upper bound of a numeric parameter.
 * <br /> 
 * This is a <i>super-lightweight</i> spec, so that:
 * <pre>
 * void m(@Max(5) int a){    }
 * </pre>
 * is as good as
 * <pre>
 * &#064;Spec( &#064;Case( pre = "a <= 5"))
 * </pre>
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Level2Desugarable( 
	pattern = "@Target <= @ValueOf(value)", 
	types = { Number.class })
public @interface Max {

	double value();
	
//	Policy policy() default Policy.INCLUSIVE;
}

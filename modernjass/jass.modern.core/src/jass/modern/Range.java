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
 * This annotation is used to specify the range
 * of numeric parameters. <br />
 * This annotation is for very lightweight specification
 * so that 
 * <pre>void m(&#064;Range(-1, 1) int a){	}</pre>
 * is equivalent to
 * <pre>
 * &#064;Spec(&#064;Case(pre="-1 &lt;= a && a &lt;= 1))
 * void m(int a){	}
 * </pre>
 * @author riejo
 */
@Documented
@Target( { ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Level2Desugarable( 
	pattern = "@ValueOf(from) <= @Target && @Target <= @ValueOf(to)", 
	types = { Number.class } )
public @interface Range {
	
	double from() default Double.MIN_VALUE;
	
//	Policy fromPolicy() default Policy.INCLUSIVE;
	
	double to() default Double.MIN_VALUE;
	
//	Policy toPolicy() default Policy.EXCLUSIVE;
	
	public enum Policy {
		
		INCLUSIVE, EXCLUSIVE;
		
		public String toString() {
			switch (this) {
			case INCLUSIVE:	return "<=";
			case EXCLUSIVE:	return "<";
			}
			
			return "";
		}
	}
}

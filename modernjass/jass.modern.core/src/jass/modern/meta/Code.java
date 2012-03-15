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
package jass.modern.meta;

import jass.modern.core.compile.transform.IAnnotationValueTransformer;
import jass.modern.core.model.IAnnotationValue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * The code-annotation is used to express that
 * the return value of method is Java code. In 
 * addition, {@link IAnnotationValueTransformer translators}
 * can be specified which transform the Java
 * code (e.g. "&#064;Return" into valid code).
 * 
 * @author riejo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Code {

	public static final class EmptyAnnotationValueTransformer implements
			IAnnotationValueTransformer {

		public void translate(IAnnotationValue value, 
				DiagnosticListener<JavaFileObject> diagnostics) {
			
			/*
			 * does nothing
			 */
		}
	}

	/**
	 * An optional set of {@link IAnnotationValueTransformer}s 
	 * which are used to transform, analyse, or whatever the
	 * code.
	 * 
	 * @return Default is the {@link EmptyAnnotationValueTransformer}
	 * 	which does nothing.
	 */
	Class<? extends IAnnotationValueTransformer>[] translator() default EmptyAnnotationValueTransformer.class;
}

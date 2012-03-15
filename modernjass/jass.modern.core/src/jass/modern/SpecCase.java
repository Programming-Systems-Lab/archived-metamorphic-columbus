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
import jass.modern.core.compile.transform.OldTransformer;
import jass.modern.core.compile.transform.QuantifierTransformer;
import jass.modern.core.compile.transform.ReturnTransformer;
import jass.modern.core.compile.transform.SignalsTransformer;
import jass.modern.meta.Code;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link SpecCase}-annotation can be used to specify the
 * behaviour of a method or constructor. The definition of
 * a specification case consists at most of three parts:
 * <ol>
 * <li>{@link #pre()}: The pre-condition of the method.
 * <li>{@link #post()}: A post-condition which must hold, if
 * 	the method returns normally (e.g. not throwing an exception)
 * <li>{@link #signalsPost()}: A post-condition which must hold, 
 * 	if the method returns throwing an exeception (see {@link #signals()}).
 * </ol>
 * In addition, {@link #preMsg()}, {@link #postMsg()}, and {@link #signalsMsg()}
 * allows to link a custom message to a condition. If a condition does not
 * hold, the accordant message is used as error message.
 * <br />
 * The {@link #visibility()}-attribute can be used to set the
 * visiblity of the specification. Two things are important when it comes
 * to visibility:
 * <ol>
 * <li>Visibility can not be larger (<i>more public</i>) than the
 * 	visibility of its target
 * <li>Elements referenced by contract must have the same or a less
 *  strict visibility. E.g. a public spec can not rely on private
 *  members:
 * <br />
 * <pre>
 * class CU {
 *  private int value;
 *  
 *  &#064;SpecCase(pre="value == 5", visibility=PUBLIC)
 *  public void m(){
 *  	//...
 *  }
 * }
 * </pre>
 * Above code will result in an compile error because the 
 * visibility of <code>value</code> and the SpecCase collide. 
 * </ol> 
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface SpecCase {
	
	/**
	 * The pre-condition of the spec-case. <em>Must be 
	 * valid Java code which evaluates to a boolean!</em>
	 * The default value is the <code>empty string</code>.
	 * @return
	 */
	@Code( translator = {ModelVariableTransformer.class, QuantifierTransformer.class } ) 
	String pre() default "";
	
	/**
	 * An error message in case the pre-condition does
	 * not hold.
	 * @return The pre-condition error message.
	 */
	String preMsg() default "";
	
	/**
	 * The post-condition of the spec-case. <em>Must be 
	 * valid Java code which evaluates to a boolean!</em>
	 * The default value is the <code>empty string</code>.
	 * @return
	 */
	@Code(translator = { ReturnTransformer.class, ModelVariableTransformer.class, 
			OldTransformer.class, QuantifierTransformer.class }) 
	String post() default "";
	
	/**
	 * An error message in case the post-condition does
	 * not hold.
	 * @return The post-condition error message.
	 */
	String postMsg() default "";
	
	/**
	 * 
	 * @return
	 */
	Class<? extends Exception> signals() default Exception.class;
	
	/**
	 * The post-condition of the spec-case in case
	 * the target (method or constructor) terminates with
	 * an exception. <em>Must be valid Java code which 
	 * evaluates to a boolean!</em> The default value is 
	 * the <code>empty string</code>.
	 * 
	 * @return
	 */
	@Code(translator = { SignalsTransformer.class, ModelVariableTransformer.class , 
			OldTransformer.class, QuantifierTransformer.class }) 
	String signalsPost() default "";
	
	/**
	 * An error message in case the exceptional post-condition
	 * does not hold.
	 * @return The exceptional post-condition.
	 */
	String signalsMsg() default "";
	
	/**
	 * 
	 * @return The visibility of this method specification.
	 */
	Visibility visibility() default Visibility.TARGET;
}

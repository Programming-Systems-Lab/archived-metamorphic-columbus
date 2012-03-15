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

import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.compile.desugar.Level1Desugarable;
import jass.modern.meta.Code;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>Pre</code>-annotation implements a 
 * <i>pre-condition</i>. <br />
 * 
 * 
 * @author riejo
 */
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@Level1Desugarable(ContractTarget.PRE)
public @interface Pre {
	
	/**
	 * A valid Java expression which evaluates
	 * to a boolean.
	 * 
	 * @return
	 */
	@Code String value();
}

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
package jass.modern.tests;

import jass.modern.Context;
import jass.modern.Invariant;
import jass.modern.SpecCase;

/**
 * Mixing static method specs and invarinats
 *
 * @author riejo
 */
public class SampleClass13 {

	@Invariant("age >= 0")
	private int age = 0;
	
	@Invariant(value = "sAge != 0", context = Context.STATIC)
	public static int sAge = 123;
	
	@SpecCase( post = "@Result > 0")
	public static int maxAge() {
		return 123;
	}
}

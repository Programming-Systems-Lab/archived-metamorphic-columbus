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

import org.junit.Before;
import org.junit.Test;

public class SampleClass4Test {

	SampleClass4 simple;
	
	@Before
	public void setUp() throws Exception {
		simple = new SampleClass4();
	}

	@Test(expected = AssertionError.class)
	public void testAdd() {
		simple.add(null);	// <- violates pre-condition obj != null
	}

	@Test(expected = AssertionError.class)
	public void testNumber1() {
		simple.number1(0);
	}
	
	@Test
	public void testNumber1Success() {
		simple.number1(1);
	}
	
	@Test(expected = AssertionError.class)
	public void testNumber2() {
		simple.number2(0);
	}
	
	@Test 
	public void testNumber2Success() {
		simple.number2(2);	// <- pre-cond form interface
		simple.number2(3);	// <- pre-cond from class
	}
	
	@Test
	public void testNumber3() {
		simple.number3(3);
	}
	
	@Test(expected = AssertionError.class)
	public void testNumber3Fails() {
		simple.number3(4);
	}
}


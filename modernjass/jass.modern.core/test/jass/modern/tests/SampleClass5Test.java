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

public class SampleClass5Test {

	SampleClass5SuperClass superSample;
	SampleClass5 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass5();
		superSample = new SampleClass5SuperClass();
	}

	@Test( expected = AssertionError.class)
	public void testNumberForFailure() {
		sample.number(23);
	}
	
	@Test
	public void testNumberForSuccess() {
		sample.number(1);	// <- pre-condition in SampleClass5
		sample.number(0);	// <- pre-condition in SampleClass5Super
	}
	
	@Test( expected = AssertionError.class)
	public void testNumberForFailure2() {
		superSample.number(0);	// <- post-condition in SampleClass5Super
	}
	
	@Test(expected = AssertionError.class)
	public void testNumber2ForFailure() {
		sample.number2(12);
	}
	
	@Test
	public void testNumber2ForSuccess() {
		superSample.number2(12);
	}
	
	@Test( expected = AssertionError.class)
	public void testM3ForFailure1() {
		sample.m3();	// <- pre-condition (protected) get inherited
	}
	
	@Test( expected = AssertionError.class)
	public void testM3ForFailure2() {
		superSample.m3();
	}
	
	@Test
	public void testM4() {
		sample.m4();
	}
}

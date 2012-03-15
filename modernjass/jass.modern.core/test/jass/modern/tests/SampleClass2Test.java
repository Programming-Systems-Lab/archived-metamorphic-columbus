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

public class SampleClass2Test {

	SampleClass2 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass2();
	}

	@Test
	public void testSqrtSuccess() {
		sample.sqrt(12);
	}
	
	@Test(expected = AssertionError.class)
	public void testSqrtFailiure() {
		sample.sqrt(-1);
	}

	@Test
	public void testAddToCount() {
		sample.addToCount(5);
		sample.addToCount(1);
		sample.addToCount(3);
	}
	
	@Test(expected = AssertionError.class)
	public void testAddToCountFail1() {
		sample.addToCount(6);
	}
	
	@Test(expected = AssertionError.class)
	public void testAddToCountFail2() {
		sample.addToCount(-1);
	}
	
	@Test(expected = AssertionError.class)
	public void testAddToCountFail3() {
		sample.fCount = -10;
		sample.addToCount(6);
	}
	
	@Test(expected = AssertionError.class)
	public void testMightThrowFails() {
		sample.mightThrow(1);
	}
	
	@Test(expected = NullPointerException.class)
	public void testMightThrowNPEExpected() {
		
		sample.mightThrow(2);
	}
	
	@Test(expected = AssertionError.class)
	public void testMightThrowNPEExpectedFails() {
		
		sample.fCount = 10;
		sample.mightThrow(2);
	}
	
	@Test
	public void testTwoNumbers() {
		sample.twoNumbers(3, 5);
	}
	
	@Test(expected = AssertionError.class)
	public void testTwoNumbersFails() {
		sample.twoNumbers(-5, 5);
	}
	
	@Test
	public void testEndlessRecursionStops() {
		sample.foo();
		sample.bar();
	}
	
	@Test( expected = AssertionError.class)
	public void testEndlessRecursionStopsCheckForFailure1() {
		sample.foo$return$value = 123;
		sample.bar();
	}
	
	@Test( expected = AssertionError.class)
	public void testEndlessRecursionStopsCheckForFailure2() {
		sample.foo$return$value = 123;
		sample.foo();
	}
}

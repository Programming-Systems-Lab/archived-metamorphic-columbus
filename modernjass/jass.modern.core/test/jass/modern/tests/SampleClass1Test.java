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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class SampleClass1Test {
	
	SampleClass1 sampleClass;
	
	@Before
	public void setUp() throws Exception {
		sampleClass = new SampleClass1();
	}

	public void testPreFailsAndHasCorrectMessage() {
		try {
			sampleClass.preFails();
			fail();
		}catch(AssertionError e) {
			assertEquals("foo", e.getMessage());
		}
	}

	@Test(expected=AssertionError.class)
	public void testPostFails() {
		sampleClass.postFails();
	}

	@Test(expected=AssertionError.class)
	public void testPostFails2() {
		sampleClass.postFails2();
	}

	@Test(expected=AssertionError.class)
	public void testPostFails3() {
		sampleClass.postFails3();
	}

	@Test
	public void testXSuccess() {
		sampleClass.preSuccess();
		sampleClass.postSuccess1();
		sampleClass.postSuccess2();
		sampleClass.postSuccess3();
		sampleClass.postSuccess4();
	}

}

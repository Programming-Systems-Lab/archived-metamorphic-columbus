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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class SampleClass3Test {

	SampleClass3 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass3(new Object());
	}

	@Test( expected = AssertionError.class)
	public void testConstructor() {
		new SampleClass3( null );
	}
	
	@Test
	public void testAddSuccess() {
		sample.add("bla");
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddFails() {
		sample.add(null);
	}

	@Test(expected = AssertionError.class)
	public void testAddFails2() {
		sample.add("foo");
		sample.add("bar");
	}
	
	@Test
	public void testClear() {
		sample.clear();
		
		sample.add("foo");
		sample.clear();
	}
	
	@Test(expected = AssertionError.class)
	public void testAddBlasFails() {
		SampleClass3.pass(new String[] {"bla1", "bla2", "bla3"});
	}
	
	@Test
	public void testReturnConstSuccess() {
		sample.returnConst();
	}
	
	@Test
	public void testReturnConstFails() {
		try {
			sample.returnConst2();
			fail();
			
		} catch (AssertionError e) {
			assertEquals("[grr, should return 1]", e.getMessage());
		}
	}
}

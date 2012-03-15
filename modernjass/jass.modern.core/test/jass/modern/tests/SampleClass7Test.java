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

import jass.modern.core.PostConditionError;
import jass.modern.core.PreConditionError;

import org.junit.Before;
import org.junit.Test;

public class SampleClass7Test {

	SampleClass7 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass7();
	}

	@Test( expected = AssertionError.class)
	public void testAddObjectFailsPost() {
		sample.add("abc");
	}
	
	@Test( expected = AssertionError.class)
	public void testAddObjectFailsPre() {
		sample.add(null);
	}

	@Test
	public void testAdd2IntObjectSuccess() {
		sample.add2(123, "123");
	}

	@Test( expected = NullPointerException.class)
	public void testAdd2IntObjectFailsNPE() {
		sample.add2(123, null);
	}
	
	@Test( expected = AssertionError.class)
	public void testAdd2IntObjectFailsPre1() {
		sample.add2(-1, "-2");
	}
	
	@Test( expected = NullPointerException.class)
	public void testAdd2IntObjectFailsPre2() {
		sample.add2(2, null);
	}
	
	@Test( expected = AssertionError.class)
	public void testAdd3ObjectFails() {
		sample.add3(null);
	}
	
	@Test
	public void testAdd4Success() {
		sample.add4("foobar");
	}
	
	@Test( expected = AssertionError.class)
	public void testAdd4Fails() {
		sample.add4("bar");
	}
	
	@Test
	public void testAdd5Success() {
		sample.add5(3);
		sample.add5(4);
		sample.add5(5);
	}
	
	@Test( expected = AssertionError.class )
	public void testAdd5Fails() {
		sample.add5(5.1);
	}
	
	@Test
	public void testAdd6Success() {
		sample.add6(8L);
		sample.add6(21232L);
	}
	
	@Test( expected = AssertionError.class)
	public void testAdd6Fails() {
		sample.add6(7L);
	}
	
	@Test( expected = PreConditionError.class )
	public void testAdd8a() {
		sample.add8a(null);
	}
	
	@Test( expected = NullPointerException.class )
	public void testAdd8() {
		sample.add8(null);
	}
	
	@Test( expected = PostConditionError.class )
	public void testAdd8false() {
		sample.add8false(null);
	}

	@Test( expected = NullPointerException.class )
	public void testAdd8b() {
		sample.add8b(null);
	}
	
	@Test( expected = PostConditionError.class)
	public void testAdd8bfalse() {
		sample.add8bfalse(null);
	}
}

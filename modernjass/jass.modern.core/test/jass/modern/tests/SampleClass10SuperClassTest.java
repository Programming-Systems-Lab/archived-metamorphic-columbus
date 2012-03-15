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

public class SampleClass10SuperClassTest {
	
	SampleClass10SuperClass c;
	SampleClass10A a;
	SampleClass10B b;
	
	@Before
	public void setUp() throws Exception {
		c = new SampleClass10SuperClass();
		a = new SampleClass10A();
		b = new SampleClass10B();
	}
	
	@Test( expected = AssertionError.class)
	public void testN() {
		c.n(1, -2);
	}
	
	@Test( expected = AssertionError.class)
	public void testNForA() {
		a.n(1, -2);
	}
	
	@Test( expected = AssertionError.class)
	public void testNForB() {
		b.n(1, -2);
	}

}

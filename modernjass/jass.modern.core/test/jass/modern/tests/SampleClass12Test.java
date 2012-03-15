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

import org.junit.Before;
import org.junit.Test;

public class SampleClass12Test {

	SampleClass12 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass12();
	}

	@Test( expected = PostConditionError.class )
	public void testM1() {
		sample.m1();
	}
	
	@Test
	public void testM2ForSuccess() {
		sample.m2(0);
	}
	
	@Test(expected = NullPointerException.class )
	public void testM2ForFailureA() {
		sample.m2(1);
	}
	
	@Test(expected = PostConditionError.class )
	public void testM2ForFailureB() {
		sample.m2(2);
	}
	
	@Test( expected = Exception.class )
	public void testM3() throws Exception {
		sample.m3();
	}
}

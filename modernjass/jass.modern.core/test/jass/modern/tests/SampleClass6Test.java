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

public class SampleClass6Test {
	
	SampleClass6 sample;
	
	@Before
	public void setUp() throws Exception {
		sample = new SampleClass6();
	}

	@Test
	public void testDoNotChange() {
		sample.doNotChange();
	}

	@Test( expected = AssertionError.class )
	public void testDoChange() {
		sample.doChange();
	}
	
	@Test( expected = AssertionError.class )
	public void testDoChangeInheritedField() {
		sample.doChangeInheritedField();
	}

	@Test
	public void testDoChangeInheritedFieldPrivateSpec() {
		sample.doChangeInheritedFieldPrivateSpec();
	}
	
//	@Test
//	public void testHelperChanges() {
//		sample.helperChanges();
//		
//		try {
//			/*
//			 * Invariant is violated now
//			 */
//			sample.doNotChange();
//			fail();
//			
//		} catch(AssertionError e) {	}
//	}
	
	@Test
	public void testCallsHelper() {
		sample.callsHelper();
	}
}

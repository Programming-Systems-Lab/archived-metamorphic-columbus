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
package jass.modern.tests.rect.old;

import static org.junit.Assert.assertEquals;
import jass.modern.core.PostConditionError;

import org.junit.Test;

public class OldSampleTest {

	@Test
	public void testTimes2() {
		int len = new OldSample().times2("ABC");
		assertEquals(3, len);
	}
	
	@Test(expected= PostConditionError.class)
	public void testTime2Faulty() {
		new OldSample().times2Faulty("ABC");
	}

}

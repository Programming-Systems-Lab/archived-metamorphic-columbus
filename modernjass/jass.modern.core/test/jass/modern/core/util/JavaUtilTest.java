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
package jass.modern.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class JavaUtilTest {

	@Test
	public void testFixArrayName() {
		fail("Not yet implemented");
	}

	@Test
	public void testFixGenericTypeName() {
		String actual = JavaUtil.fixGenericTypeName("java.lang.List<java.lang.String>");
		assertEquals("java.lang.List", actual);
		
		actual = JavaUtil.fixGenericTypeName("java.lang.List<java.lang.Map<Integer, String>>");
		assertEquals("java.lang.List", actual);
	}

	@Test
	public void testIsJavaIdentifier() {
		assertFalse(JavaUtil.isJavaIdentifier("12abc"));
		assertTrue(JavaUtil.isJavaIdentifier("$abc"));
		assertTrue(JavaUtil.isJavaIdentifier("_j34"));
	}

}

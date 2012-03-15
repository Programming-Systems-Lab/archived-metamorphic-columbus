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
package jass.modern.core.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;
import jass.modern.tests.SampleClass6;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TypeTest {

	IType fType;
	IType fTypeClone;
	
	@Before
	public void setupType() throws IOException {
		fType = new TypeFactory(false).createType(
				Helper.openInputStream(SampleClass6.class));
		
		assertNotNull(fType);
		fTypeClone = (IType) fType.clone();
	}
	
	@Test
	public void testClonedType() {
		assertTrue(fTypeClone != fType);

		assertEquals(7, Elements.filter(IExecutable.class, fTypeClone.getEnclosedElements()).size());
		assertEquals(7, Elements.filter(IExecutable.class, fType.getEnclosedElements()).size());
	}

	@Test
	public void testMembersAreIndependent() {
		fTypeClone.addEnclosedElement(new ExecutableElement("foo", "void"));
		fTypeClone.addEnclosedElement(new ExecutableElement("bar", "void"));

		assertEquals(9, Elements.filter(IExecutable.class, fTypeClone.getEnclosedElements()).size());
		assertEquals(7, Elements.filter(IExecutable.class, fType.getEnclosedElements()).size());
	}
	
	@Test
	public void testShallowCopyOnly() {
		IExecutable e1 = Elements.filterFirst("helperChanges", IExecutable.class, fTypeClone.getEnclosedElements());
		IExecutable e2 = Elements.filterFirst("helperChanges", IExecutable.class, fType.getEnclosedElements());
		
		assertTrue(e1 == e2);
	}
}

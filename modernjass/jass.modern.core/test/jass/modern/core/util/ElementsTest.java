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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jass.modern.Visibility;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.AnnotationValue;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.tests.Helper;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ElementsTest {

	class A {
		
		public void m() {
			
		}
		
		protected void n() {
			
		}
		
		private void o() {
			
		}
	}
	
	interface B {
		void p();
	}
	
	class C extends A implements B {

		@Override
		public void p() {
		
		}
		
		protected void n() {
			
		}
		
		private void q() {
			
		}
	}
	
	IType typeC;
	IExecutable methodP;
	IExecutable methodN;
	IExecutable methodQ;
	
	IAnnotationValue fValueEnum;
	
	@Before
	public void setUp() throws Exception {
		typeC = new TypeFactory(false).createType(Helper.openInputStream(C.class));
		assertNotNull(typeC);
		
		methodP = Elements.filterFirst("p", IExecutable.class, typeC.getEnclosedElements());
		methodN = Elements.filterFirst("n", IExecutable.class, typeC.getEnclosedElements());
		methodQ = Elements.filterFirst("q", IExecutable.class, typeC.getEnclosedElements());
		
		assertNotNull(methodP);
		assertNotNull(methodN);
		assertNotNull(methodQ);
		
		fValueEnum = new AnnotationValue("visibility");
		fValueEnum.setValue(Visibility.PRIVATE);
	}

	@Test
	public void testGetParent() {
		IType parent = Elements.getParent(IType.class, typeC);
		assertTrue(parent == null);
		
		for(IElement element : typeC.getEnclosedElements()) {
			parent = Elements.getParent(IType.class, element);
			assertTrue(typeC == parent);
		}
	}

	@Test
	public void testGetAllMembers() {
		List<IElement> members = Elements.getAllMembers(typeC);
		int privateCount = 0;
		for (IElement element : members) {
			
			if(element.getModifiers().contains(Modifier.PRIVATE))
				privateCount += 1;
		}
		
		assertEquals(privateCount, 1); // and that's C.q()
	}

	@Test
	public void testGetAllSuperTypes() {
		Collection<IType> supertypes = Elements.getAllSuperTypes(typeC);
		assertEquals(3, supertypes.size());
	}

	@Test
	public void testGetVisibility() {
		assertEquals(Visibility.PACKAGE_PRIVATE, Elements.getVisibility(typeC));
	}

	@Test
	public void testOverrides() {
		assertTrue(Elements.overrides(typeC, methodP));
		assertTrue(Elements.overrides(typeC, methodN));
		assertFalse(Elements.overrides(typeC, methodQ));
	}
	
	@Test
	public void testIsValidTypeName() {
		assertTrue(Elements.isTypeName("java.lang.Object"));
		assertTrue(Elements.isTypeName("jass.modern.Annotation$InternalVisitor$1"));
		assertTrue(Elements.isTypeName("Foo_1223Hh"));
		
		assertFalse(Elements.isTypeName("java.lang._Object"));
		assertFalse(Elements.isTypeName(".java.lang.String"));
		assertFalse(Elements.isTypeName("java.lang.String<E>"));
	}
	
	@Test
	public void testGetValue() {
		Visibility v = Elements.getValue(fValueEnum, Visibility.class);
		assertNotNull(v);
	}
}

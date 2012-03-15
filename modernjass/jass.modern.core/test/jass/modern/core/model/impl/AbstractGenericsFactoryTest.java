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
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.util.Elements;
import jass.modern.tests.ClassWithGenerics;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractGenericsFactoryTest {
	
	IType testType;
	Class<?> type;
	
	IVariable fieldMap;
	
	IExecutable methodTransform;
	IExecutable methodMultiply;
	IExecutable methodDoStuff;
	
	@Before
	abstract void createType() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		type = ClassWithGenerics.class;
		createType();
		assertNotNull(testType);
		
		fieldMap = Elements.filterFirst("map", IVariable.class, testType.getEnclosedElements());
		assertNotNull(fieldMap);
		
		methodTransform = Elements.filterFirst("transform", IExecutable.class, 
				testType.getEnclosedElements());
		assertNotNull(methodTransform);
		
		methodMultiply = Elements.filterFirst("multiply", IExecutable.class,
				testType.getEnclosedElements());
		assertNotNull(methodMultiply);
		
		methodDoStuff = Elements.filterFirst("doStuff", IExecutable.class,
				testType.getEnclosedElements());
		assertNotNull(methodDoStuff);
	}
	
	@Test
	public void quickTypeTest() {
		List<IExecutable> elements = Elements.filter(IExecutable.class, testType.getEnclosedElements());
		assertEquals(4, elements.size());
		
		// test  type
		List<String> list = testType.getGenericSignature();
		assertEquals(4, list.size());
		assertEquals("A extends java.lang.Number", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
		assertEquals("D", list.get(3));
		
		// test interfaces
		list = testType.getInterfaces();
		assertEquals(2, list.size());
		assertEquals("jass.modern.tests.SuperInterfaceWithGenerics1", list.get(0));
		assertEquals("jass.modern.tests.SuperInterfaceWithGenerics2", list.get(1));
		assertEquals("jass.modern.tests.SuperClassWithGenerics", testType.getSuperclass());
	}
	
	@Test
	public void testFieldMap() {
		assertEquals(2, fieldMap.getGenericSignature().size());
		
		assertEquals("java.lang.String", fieldMap.getGenericSignature().get(0));
		assertEquals("java.lang.Integer", fieldMap.getGenericSignature().get(1));
	}
	
	@Test
	public void testMethodTransform() {

		assertEquals("T", methodTransform.getReturnType());
		
		assertEquals(1, methodTransform.getGenericSignature().size());
		assertEquals("T", methodTransform.getGenericSignature().get(0));
		
		List<IVariable> parameters = methodTransform.getParameters();
		assertEquals(1, parameters.size());
		IVariable parameter = parameters.get(0);
		assertNotNull(parameter);
		assertEquals("type", parameter.getSimpleName());
		assertEquals("T", parameter.getType());
	}
	
	@Test
	public void testMethodMultiply() {
		assertEquals("java.lang.Number", methodMultiply.getReturnType());
		
		assertEquals(1, methodMultiply.getGenericSignature().size());
		assertEquals("T extends java.lang.Number", methodMultiply.getGenericSignature().get(0));
		
		List<IVariable> parameters = methodMultiply.getParameters();
		assertEquals(3, parameters.size());
		
		assertEquals("T", parameters.get(0).getType());
		assertEquals("T", parameters.get(1).getType());
		assertEquals("double", parameters.get(2).getType());
	}
	
	@Test
	public void testMethodDoStuff() {
		assertEquals("A", methodDoStuff.getReturnType());
		
		assertEquals(0, methodDoStuff.getGenericSignature().size());
		
		assertEquals(1, methodDoStuff.getParameters().size());
		assertEquals("java.util.List<java.util.List<A>>", methodDoStuff.getParameters().get(0).getType());
	}
}

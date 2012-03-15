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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.impl.Annotation;
import jass.modern.core.model.impl.AnnotationValue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ContractsTest {

	IAnnotation fAnnotation;
	Object defaultObj = new Object();
	
	@Before
	public void setUp() throws Exception {
		fAnnotation = new Annotation("jass.modern.Sample");
		new AnnotationValue(fAnnotation, "nullValue", (Object) null);
		new AnnotationValue(fAnnotation, "intValue", 1);
		new AnnotationValue(fAnnotation, "stringValue", "foo");
		new AnnotationValue(fAnnotation, "stringValueS", "foo", "bar");
		new AnnotationValue(fAnnotation, "nestedAnnotation", new Annotation("bla"));
		new AnnotationValue(fAnnotation, "classValue", Exception.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetValue() {
		Object actual = Elements.getValue(fAnnotation, "intValue", Integer.class);
		assertNotNull(actual);
		assertEquals(1, actual);
		
		actual = Elements.getValue(fAnnotation, "nullValue", Object.class);
		assertNull(actual);
		
		actual = Elements.getValue(fAnnotation, "stringValue", String.class);
		assertNotNull(actual);
		assertEquals("foo", actual);
		
		actual = Elements.getValue(fAnnotation, "stringValue", List.class);
		assertNotNull(actual);
		assertTrue(actual instanceof List);
		assertEquals(1, ((List)actual).size());
		
		actual = Elements.getValue(fAnnotation, "stringValueS", List.class);
		assertNotNull(actual);
		assertTrue(actual instanceof List);
		List<String> valueList = (List<String>) actual;
		assertTrue(valueList.contains("foo"));
		assertTrue(valueList.contains("bar"));
		
		actual = Elements.getValue(fAnnotation, "nestedAnnotation", IAnnotation.class);
		assertNotNull(actual);
		assertEquals("bla", ((IAnnotation) actual).getSimpleName());
	}

	
	@Test
	public void testClassValue() {
		Object actual = Elements.getValue(fAnnotation, "classValue", Throwable.class.getClass());
		assertNotNull(actual);
		assertTrue(actual == Exception.class);
	}
	
	@Test
	public void testGetClass() {
		IAnnotation anno = new Annotation("jass.modern.Pre");
		Class<? extends java.lang.annotation.Annotation> clsAnno = Contracts.getClass(anno);
		
		assertNotNull(clsAnno);
	}
	
	@Test
	public void testGetContractTarget() {
		IAnnotation ann;
		ann = new Annotation("jass.modern.Pre");
		assertTrue(ContractTarget.PRE == Contracts.getContractTarget(ann));
		
		ann = new Annotation("jass.modern.Post");
		assertTrue(ContractTarget.POST == Contracts.getContractTarget(ann));
	}
}

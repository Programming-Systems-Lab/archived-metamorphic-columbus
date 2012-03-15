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
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.IType.Kind;
import jass.modern.core.util.Elements;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractFactoryTest {

	protected IType fClassType;

	protected IType fInterfaceType;
	
	public AbstractFactoryTest() {
		super();
	}
	
	@Before
	public abstract void setUpClassType() throws Exception;
	
	@Before
	public abstract void setUpInterfaceType() throws Exception;
	
	@Test
	public void testInterfaceType() {
		// (1) check type
		assertNotNull(fInterfaceType);
		assertTrue(fInterfaceType.getKind() == Kind.INTERFACE);
		
		// (2) check super class
		assertEquals("java.lang.Object", fInterfaceType.getSuperclass());
		
		// (3) check interfaces
		assertNotNull(fInterfaceType.getInterfaces());
		assertEquals(1, fInterfaceType.getInterfaces().size());
		assertEquals("jass.modern.core.model.impl.IFacSampleB", fInterfaceType.getInterfaces().get(0));
		assertTrue(fInterfaceType.getModifiers().contains(Modifier.ABSTRACT));
	}
	
	@Test
	public void testSpecNameInInterfaceType() {
		IExecutable m = (IExecutable) Elements.filter("foo", IExecutable.class, 
				fInterfaceType.getEnclosedElements()).get(0);
		assertNotNull(m);
		assertEquals(1, m.getParameters().size());
	}
	
	@Test
	public void testType() throws Exception {
		
		assertNotNull(fClassType);
		assertTrue(fClassType.getKind() == Kind.CLASS);
		assertEquals("DummyBuffer", fClassType.getSimpleName());
		assertEquals("jass.modern.tests.DummyBuffer", fClassType.getQualifiedName());
		
		assertEquals("java.lang.Object", fClassType.getSuperclass());
		assertTrue(!fClassType.getInterfaces().isEmpty());
		assertEquals("jass.modern.tests.IDummyInterface", fClassType.getInterfaces().get(0));
		
		assertEquals(3, Elements.filter(IVariable.class, fClassType.getEnclosedElements()).size());
		assertEquals(5, Elements.filter(IExecutable.class, fClassType.getEnclosedElements()).size());
		
		assertEquals("java.lang.Object", fClassType.getSuperclass());
		assertTrue(!fClassType.getModifiers().contains(Modifier.ABSTRACT));
	}

	@Test
	public void testTypeAnnotations() {
		
		List<IAnnotation> annotations = Elements.filter(IAnnotation.class, fClassType.getEnclosedElements());
		assertEquals(1, annotations.size());
		
		IAnnotation annotation = annotations.get(0);
		assertEquals("jass.modern.Model", annotation.getSimpleName());
		
		IAnnotationValue typeValue = annotation.getValue("type");
		assertNotNull(typeValue);
		Class<?> clazz = Elements.getValue(typeValue, Class.class);
		assertNotNull(clazz);
		assertTrue(Object[].class == clazz);
	}
	
	@Test
	public void testConstructor() {
		IExecutable c = (IExecutable) Elements.filter("DummyBuffer", IExecutable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(c);
		assertEquals(1, c.getParameters().size());
	}
	
	@Test
	public void testMethodAdd() {
		IElement element = Elements.filter("add", IExecutable.class, fClassType.getEnclosedElements()).get(0);
			
		assertEquals(1, ((IExecutable)element).getParameters().size());
		assertEquals(1, Elements.filter(IAnnotation.class, element.getEnclosedElements()).size());
	}

	@Test 
	public void testAnnotationOnMethodAdd() {
		IElement element = Elements.filter("add", IExecutable.class, fClassType.getEnclosedElements()).get(0);		
		
		IAnnotation annotation = (IAnnotation) Elements.filter("jass.modern.Pre", IAnnotation.class, element.getEnclosedElements()).get(0);
		assertNotNull(annotation);
		assertEquals("obj != null", annotation.getValue("value").getValue().get(0));
	}
	
	public void testGetDefaultValue() {
		IElement element = Elements.filter("DummyBuffer", IExecutable.class, fClassType.getEnclosedElements()).get(0);
		IAnnotation annotation = (IAnnotation) Elements.filter("jass.modern.Also", IAnnotation.class, element.getEnclosedElements());
		IAnnotationValue nestedAnnotation = (IAnnotationValue) Elements.filter(
				"value", IAnnotationValue.class, annotation.getEnclosedElements());
		assertNotNull(nestedAnnotation);
		assertTrue(nestedAnnotation == annotation.getDefaultValue());
	}
	
	@Test
	public void testNestedAnnotation() {
		IElement element = Elements.filter("DummyBuffer", IExecutable.class, fClassType.getEnclosedElements()).get(0);
		
		IAnnotation annotation = (IAnnotation) Elements.filter("jass.modern.Also", 
				IAnnotation.class, element.getEnclosedElements()).get(0);
		assertNotNull(annotation);
		assertEquals("jass.modern.Also", annotation.getSimpleName());
		assertEquals(1, annotation.getEnclosedElements().size());
		
		IAnnotationValue nestedAnnotation = annotation.getDefaultValue();
		
		assertEquals("value", nestedAnnotation.getSimpleName());
		assertTrue(nestedAnnotation.getValue() instanceof List);
		
		Object[] obj = nestedAnnotation.getValue().toArray();
		assertEquals(2, obj.length);
		
		assertTrue(obj[0] instanceof IAnnotation);
		assertEquals("jass.modern.SpecCase", ((IAnnotation)obj[0]).getSimpleName());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotationValueIsClass() {
		IElement element = Elements.filter("DummyBuffer", IExecutable.class, fClassType.getEnclosedElements()).get(0);
		IAnnotation annotation = (IAnnotation) Elements.filter("jass.modern.Also", 
				IAnnotation.class, element.getEnclosedElements()).get(0);
		
		List<IAnnotation> cases = Elements.getValue(annotation, "value", List.class);
		assertEquals(2, cases.size());
		
		IAnnotation signalsCase = cases.get(0);
		Class cls = Elements.getValue(signalsCase, "signals", Class.class);
		assertNotNull(cls);
	}
	
	@Test
	public void testMethodSignatureIsFull() {
		IExecutable element = (IExecutable) Elements.filter("isFull",IExecutable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(element);
		assertEquals("boolean", element.getReturnType());
		assertEquals("isFull", element.getSimpleName());
		
		assertEquals(2, element.getParameters().size());
		assertEquals(2, element.getExceptions().size());
	}
	
	@Test
	public void testMethodMainHasArrays() {
		IExecutable element = (IExecutable) Elements.filter("main",IExecutable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(element);
		assertEquals(1, element.getParameters().size());
		IVariable param1 = element.getParameters().get(0);
		
		assertNotNull(param1);
		assertEquals("java.lang.String[][]", param1.getType());
		assertEquals("java.lang.Object[]", element.getReturnType());
	}
	
	@Test
	public void testStaticMethod() {
		IExecutable element = (IExecutable) Elements.filter("staticMain",IExecutable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(element);
		
		assertTrue(element.getModifiers().contains(Modifier.STATIC));
		
		assertEquals(1, element.getParameters().size());
		assertEquals("args", element.getParameters().get(0).getSimpleName());
	}

	@Test
	public void testAnnotatedField() {
		IVariable var = (IVariable) Elements.filter("fNotNullObject", IVariable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(var);
		assertEquals("java.lang.Object", var.getType());
		
		List<? extends IElement> contracts =  Elements.filter(IAnnotation.class, var.getEnclosedElements());
		assertNotNull(contracts);
		assertEquals(1, contracts.size());
		
		IAnnotation contract = (IAnnotation) contracts.get(0);
		assertEquals("fNotNullObject != null", contract.getDefaultValue().getValue().get(0));
	}
	
	@Test
	public void testStaticField() {
		IVariable var = (IVariable) Elements.filter("staticField", IVariable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(var);
		assertEquals("java.lang.reflect.Field", var.getType());
		
		assertTrue(var.getModifiers().contains(Modifier.STATIC));
	}
	
	@Test
	public void testFieldVisibility() {
		IVariable var = (IVariable) Elements.filter("staticField", IVariable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(var);
		assertTrue(var.getModifiers().contains(Modifier.PACKAGE_PRIVATE));
		
		var = (IVariable) Elements.filter("fNotNullObject", IVariable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(var);
		assertTrue(var.getModifiers().contains(Modifier.PRIVATE));
		
		var = (IVariable) Elements.filter("fPrimitiveInt", IVariable.class, fClassType.getEnclosedElements()).get(0);
		assertNotNull(var);
		assertTrue(var.getModifiers().contains(Modifier.PUBLIC));
	}
	
	@Test
	public void testMethodVisibility() {
		IExecutable exec;
		
		exec = Elements.filterFirst("add", IExecutable.class, fClassType.getEnclosedElements());
		assertTrue(exec.getModifiers().contains(Modifier.PUBLIC));
		
		exec = Elements.filterFirst("isFull", IExecutable.class, fClassType.getEnclosedElements());
		assertTrue(exec.getModifiers().contains(Modifier.PACKAGE_PRIVATE));
		
		exec = Elements.filterFirst("main", IExecutable.class, fClassType.getEnclosedElements());
		assertTrue(exec.getModifiers().contains(Modifier.PROTECTED));
		
		exec = Elements.filterFirst("staticMain", IExecutable.class, fClassType.getEnclosedElements());
		assertTrue(exec.getModifiers().contains(Modifier.PRIVATE));
		
	}
	
	@Test
	public void testParameterAnnotations() {
		IExecutable methodIsFull = Elements.filterFirst("isFull", IExecutable.class, fClassType.getEnclosedElements());
		assertNotNull(methodIsFull);
		
		List<IVariable> parameters = methodIsFull.getParameters();
		assertEquals(2, parameters.size());
		
		IVariable paramObj = parameters.get(0);
		assertEquals("o", paramObj.getSimpleName());
		
		IAnnotation notNullAnnotation = Elements.filterFirst("jass.modern.NonNull", 
				IAnnotation.class, paramObj.getEnclosedElements());
		assertNotNull(notNullAnnotation);
		
		IVariable paramInt = parameters.get(1);
		assertEquals("i", paramInt.getSimpleName());
		
		IAnnotation minAnnotation = Elements.filterFirst("jass.modern.Min", 
				IAnnotation.class, paramInt.getEnclosedElements());
		assertNotNull(minAnnotation);
		
		Double value = Elements.getDefaultValue(minAnnotation, Double.class);
		assertNotNull(value);
		assertEquals(12, value.intValue());
	}
}

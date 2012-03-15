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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jass.modern.core.apt.IRevealableAnnotationValue;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.util.Elements;
import jass.modern.tests.DummyBuffer;
import jass.modern.tests.Helper;

import org.junit.Before;
import org.junit.Test;

public class TypeFactoryJavaxLangModelTest extends AbstractFactoryTest {

	@Override
	@Before
	public void setUpClassType() throws Exception {

		TypeFactoryJavaxLangModel.InternalElementVisitor visitor = new TypeFactoryJavaxLangModel().new InternalElementVisitor();
		AnnotationProcessingTestUtil.runProcessor(new AnnotationProcessingTestUtil.InternalAnnotationProcessor(visitor, null), 
				Helper.getAbsoluteFilePath(DummyBuffer.class)[0]);
		
		fClassType = visitor.getType();
	}

	@Override
	@Before
	public void setUpInterfaceType() throws Exception {
		
		TypeFactoryJavaxLangModel.InternalElementVisitor visitor = new TypeFactoryJavaxLangModel().new InternalElementVisitor();
		AnnotationProcessingTestUtil.runProcessor(new AnnotationProcessingTestUtil.InternalAnnotationProcessor(visitor, null), 
				Helper.getAbsoluteFilePath(IFacSample.class)[0]);
		
		fInterfaceType = visitor.getType();
	}


	@Test
	public void checkAnnotationReference() {
		IElement element = Elements.filter("add", IExecutable.class, fClassType.getEnclosedElements()).get(0);
		IAnnotation annotation = (IAnnotation) Elements.filter("jass.modern.Pre", 
				IAnnotation.class, element.getEnclosedElements()).get(0);
		
		IAnnotationValue defaultValue = annotation.getValue("value");
		assertNotNull(defaultValue);
		assertTrue(defaultValue instanceof IRevealableAnnotationValue);
		
		IRevealableAnnotationValue rValue = (IRevealableAnnotationValue) defaultValue;
		assertNotNull(rValue.getElement());
		assertNotNull(rValue.getAnnotationMirror());
		assertNotNull(rValue.getAnnotationValue());
	}
}

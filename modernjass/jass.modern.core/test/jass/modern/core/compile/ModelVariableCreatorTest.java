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
package jass.modern.core.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import jass.modern.Model;
import jass.modern.core.compile.creation.ContractCreationController;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.AnnotationValue;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;

import java.io.IOException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ModelVariableCreatorTest {
	
	
	@Model(name = "foo", type = Integer.class )
	class ModelVarType {
		
	}
	
	DiagnosticCollector<JavaFileObject> diagnostics;
	IType type;
	
	@Before
	public void setUp() throws IOException {
		diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		type = new TypeFactory(false).createType(Helper.openInputStream(ModelVarType.class));
		assertNotNull(type);
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testCreate() {
		IAnnotation annotation = Elements.filterFirst(IAnnotation.class, type.getEnclosedElements());
		assertNotNull(annotation);
		
		type.accept(new ContractCreationController(), diagnostics);
		
		assertEquals(diagnostics.getDiagnostics().size(), 1);
		assertEquals("Type jass.modern.core.compile.ModelVariableCreatorTest.ModelVarType " +
				"must be abstract or foo must be represented.", 
				diagnostics.getDiagnostics().get(0).getMessage(null));
	}
	
	@Test
	public void testIllegalName() {
		IAnnotation annotation = Elements.filterFirst(IAnnotation.class, type.getEnclosedElements());
		assertNotNull(annotation);
		
		IAnnotationValue value = annotation.getValue("name");
		annotation.removeEnclosedElement(value);
		
		value = new AnnotationValue(annotation, "name", "1foo");
		
		type.accept(new ContractCreationController(), diagnostics);
		
		assertEquals("1foo is not a valid Java identifier.", diagnostics.getDiagnostics().get(0).getMessage(null));
	}

}

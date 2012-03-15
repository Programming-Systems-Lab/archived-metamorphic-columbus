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
package jass.modern.core.compile.desugar;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;
import jass.modern.tests.DesugaringSample;
import jass.modern.tests.Helper;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.Before;
import org.junit.Test;

public class DesugaringLevel2VisitorTest {

	IType fType;
	IVariable fieldAge;
	IVariable fieldName;
	IExecutable methodM3;
	IExecutable methodM4;
	IExecutable methodM5;
	
	DesugaringLevel2Visitor visitor2 = new DesugaringLevel2Visitor();
	
	@Before
	public void setUp() throws Exception {
		fType = new TypeFactory(false).createType(Helper.openInputStream(DesugaringSample.class));
		fType.accept(visitor2, new DiagnosticCollector<JavaFileObject>());
		
		fieldAge = Elements.filterFirst("fAge", IVariable.class, fType.getEnclosedElements());
		fieldName = Elements.filterFirst("fName", IVariable.class, fType.getEnclosedElements());
		methodM3 = Elements.filterFirst("m3", IExecutable.class, fType.getEnclosedElements());
		methodM4 = Elements.filterFirst("m4", IExecutable.class, fType.getEnclosedElements());
		methodM5 = Elements.filterFirst("m5", IExecutable.class, fType.getEnclosedElements());

		assertNotNull(fieldName);
		assertNotNull(methodM3);
		assertNotNull(methodM4);
		assertNotNull(methodM5);
	}
	
	@Test
	public void testField() {
	
		List<IAnnotation> annotations = Contracts.getInvariants(fType);
		IAnnotation invariant = annotations.get(0);
		assertEquals("jass.modern.Invariant", invariant.getSimpleName());
		assertEquals("(java.lang.Object) fName != null", Elements.getDefaultValue(invariant, String.class));
	}

	@Test
	public void testAge() {
		
		List<IAnnotation> annotations = Contracts.getInvariants(fType);
		IAnnotation invariant = annotations.get(1);
		assertEquals("jass.modern.Invariant", invariant.getSimpleName());
		assertEquals("fAge <= 100.0", Elements.getDefaultValue(invariant, String.class));
	}
	
	@Test
	public void testMethodM4() {
		List<IAnnotation> specs = Contracts.getSpecCases(methodM4);
		assertEquals(1, specs.size());
		
		String actual = Elements.getValue(specs.get(0), "post", String.class);
		assertEquals("(java.lang.Object) @Result != null", actual);
	}

	@Test
	public void testMethodM5() {
		List<IAnnotation> specs = Contracts.getSpecCases(methodM5);
		assertEquals(2, specs.size());
		
		String[] pre = { "(java.lang.Object) o != null", "(java.lang.Object) o != null && o == null" };
		
		for(int i = 0; i<2; i++) {
			String actual = Elements.getValue(specs.get(i), "pre", String.class);
			assertEquals(pre[i], actual);
		}
	}
}

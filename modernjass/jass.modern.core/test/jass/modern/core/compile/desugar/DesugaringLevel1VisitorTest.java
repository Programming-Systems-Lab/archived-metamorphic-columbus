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
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;
import jass.modern.tests.DesugaringSample;
import jass.modern.tests.Helper;

import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class DesugaringLevel1VisitorTest {

	protected IType fType;
	
	IExecutable methodM1;
	IExecutable methodM2;

	DesugaringLevel1Visitor visitor = new DesugaringLevel1Visitor();

	@Before
	public void setUp() throws Exception {
		fType = new TypeFactory(false).createType(Helper.openInputStream(DesugaringSample.class));
		fType.accept(visitor, null);
		
		methodM1 = Elements.filterFirst("m1", IExecutable.class, fType.getEnclosedElements());
		methodM2 = Elements.filterFirst("m2", IExecutable.class, fType.getEnclosedElements());
		
		assertNotNull(methodM1);
		assertNotNull(methodM2);
	}
	
	@Test
	public void testM1() {
		List<IAnnotation> specs = Contracts.getSpecCases(methodM1);
		
		assertNotNull(specs);
		assertEquals(1, specs.size());
		
		IAnnotation specCase = specs.get(0);
		
		String pre = Elements.getValue(specCase, "pre", String.class);
		assertEquals("o != null", pre);
		
		String post = Elements.getValue(specCase, "post", String.class);
		assertEquals("true", post);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testM2() {
		List<IAnnotation> spec = Contracts.getSpecCases(methodM2);
		
		assertNotNull(spec);
		assertEquals(4, spec.size());
		
		String[] pre = { "true", "o == null", "len >= 0", "o != null" };
		String[] post = { "true", null, null, "false"};
		
		for(int i = 0; i<4; i++) {
			String actual = Elements.getValue(spec.get(i), "pre", String.class);
			assertEquals(pre[i], actual);
			
			actual = Elements.getValue(spec.get(i), "post", String.class);
			assertEquals(post[i], actual);
		}
	}

}

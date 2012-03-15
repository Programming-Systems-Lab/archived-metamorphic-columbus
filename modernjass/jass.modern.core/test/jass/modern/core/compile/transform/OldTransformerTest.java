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
package jass.modern.core.compile.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jass.modern.Model;
import jass.modern.Post;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.Before;
import org.junit.Test;

public class OldTransformerTest {
	
	class A {
		
		int abc = 123;
		
		String abc() {
			return "abc";
		}
		
		private Object v() {
			return null;
		}
	}
	
	@Model( name = "foo", type = A.class)
	class SampleType extends A {
		
		long n = 0;
		
		int v() {
			return 1;
		}
		
		@Post("@Old(n) < n && @Old(v(), int) >= 1")
		void m() {
			n += 1;
		}
		
		@Post("@Old(abc) == 123 && @Old(abc()).equals(\"abc\")")
		void n() {	}
		
		@Post("@Old(foo) != null")
		void bar() {
			
		}
		
	}
	
	DiagnosticCollector<JavaFileObject> collector;
	OldTransformer translator;
	IAnnotationValue valueM;
	IAnnotationValue valueN;
	IAnnotationValue valueBar;
	
	private IType fType;
	
	@Before
	public void setUp() throws Exception {
		translator = new OldTransformer();
		collector = new DiagnosticCollector<JavaFileObject>();
		
		fType = new TypeFactory(false).createType(
				Helper.openInputStream(SampleType.class));
		IExecutable m = Elements.filterFirst("m", IExecutable.class, fType.getEnclosedElements());
		IAnnotation a = Elements.filterFirst(IAnnotation.class, m.getEnclosedElements());
		valueM = a.getDefaultValue();
		assertNotNull(valueM);
		
		IExecutable n = Elements.filterFirst("n", IExecutable.class, fType.getEnclosedElements());
		a = Elements.filterFirst(IAnnotation.class, n.getEnclosedElements());
		valueN = a.getDefaultValue();
		assertNotNull(valueN);
		
		IExecutable bar = Elements.filterFirst("bar", IExecutable.class, fType.getEnclosedElements());
		a = Elements.filterFirst(IAnnotation.class, bar.getEnclosedElements());
		valueBar = a.getDefaultValue();
		assertNotNull(valueBar);
	}

	@Test
	public void testTranslate() {
		translator.translate(valueM, collector);
		
		String code = Elements.getValue(valueM, String.class);
		assertTrue(code.contains("(java.lang.Long) _Context.old(\"n\")"));
		assertTrue(code.contains("(java.lang.Integer) _Context.old(\"vLR\")"));
		
		List<IContractExecutable> list = Elements.filter("m$old$*", 
				IContractExecutable.class, fType.getEnclosedElements());
		assertEquals(2, list.size());
		assertTrue(list.get(0).getCode().contains("n"));
		assertTrue(list.get(1).getCode().contains("v()"));
	}
	
	@Test
	public void testTypeInference() {
		translator.translate(valueN, collector);
		
		String code = Elements.getValue(valueN, String.class);
		assertTrue(code.contains("(java.lang.Integer) _Context.old(\"abc\")"));
		assertTrue(code.contains("(java.lang.String) _Context.old(\"abcLR\""));
	}
	
	@Test
	public void testModelVarInference() {
		translator.translate(valueBar, collector);

		String code = Elements.getValue(valueBar, String.class);
		assertTrue(code.contains("(jass.modern.core.compile.transform.OldTransformerTest$A) _Context.old(\"foo\")"));
	}
}

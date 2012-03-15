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


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import jass.modern.Invariant;
import jass.modern.Model;
import jass.modern.ModelDefinitions;
import jass.modern.SpecCase;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.creation.ModelVariableHelper;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.Before;
import org.junit.Test;

public class ModelVariableTransformerTest {

	@ModelDefinitions({
		@Model( name = "foo", type = Integer.class),
		@Model( name = "bar", type = Double.class)	})
	class SomeClass {
		
		@Invariant("0 <= foo && foo <= 12 || bar == @Old(bar)")
		Object o;
		
		@SpecCase( 
			pre = "bar.equals(bar)", 
			post = "this.bar.equals(bar)")
		public void m(Double bar) {
			
		}
	}
	
	IType type;
	IAnnotationValue valueInvariant;
	
	IAnnotationValue valuePre;
	IAnnotationValue valuePost;
	
	ModelVariableHelper helper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	private ModelVariableTransformer fTransformer;
	
	@Before
	public void setUp() throws Exception {

		fTransformer = new ModelVariableTransformer();
		
		type = new TypeFactory(false).createType(Helper.openInputStream(SomeClass.class));
		assertNotNull(type);
		
		IVariable varO = Elements.filterFirst("o", IVariable.class, type.getEnclosedElements());
		IAnnotation annotation = Elements.filterFirst(IAnnotation.class, varO.getEnclosedElements());
		valueInvariant = annotation.getDefaultValue();
		assertNotNull(valueInvariant);
		
		IExecutable exec = Elements.filterFirst("m", IExecutable.class, type.getEnclosedElements());
		annotation = Elements.filterFirst(IAnnotation.class, exec.getEnclosedElements());
		
		valuePre = annotation.getValue("pre");
		valuePost = annotation.getValue("post");
		assertNotNull(valuePre);
		assertNotNull(valuePost);
	}

	@Test
	public void testTranslateInvariant() {

		fTransformer.translate(valueInvariant, new DiagnosticCollector<JavaFileObject>());

		String code = Elements.getValue(valueInvariant, String.class);
		assertEquals("0 <= foo$model() && foo$model() <= 12 || bar$model() == @Old(bar$model())", code);
	}
	
	@Test
	public void testTranslateSpecCasePre() {
		
		fTransformer.translate(valuePre, new DiagnosticCollector<JavaFileObject>());
		String code = Elements.getValue(valuePre, String.class);
		
		assertEquals("bar.equals(bar)", code);
	}
	
	@Test
	public void testTranslateSpecCasePost() {
		
		fTransformer.translate(valuePost, new DiagnosticCollector<JavaFileObject>());
		String code = Elements.getValue(valuePost, String.class);
		
		assertEquals("this.bar$model().equals(bar)", code);
	}
}

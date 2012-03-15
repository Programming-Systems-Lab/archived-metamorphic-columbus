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
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;

import org.junit.Before;
import org.junit.Test;

public class QuantifierTransformerTest {

	class SomeClass {
	
		@Invariant("@ForAll(Object o : o4; o.toString() != null) && @Exists(Object o : o4; o.toString() != null)")
		Object[] o3;
		
		@Invariant("@ForAll(Object o:o4; o.toString() != null)")
		Object[] o4;

	}
	
	IType type;
	IAnnotationValue value3;
	IAnnotationValue value4;
	QuantifierTransformer transformer;
	
	@Before
	public void setUp() throws Exception {
		type = new TypeFactory(false).createType(Helper.openInputStream(SomeClass.class));
		assertNotNull(type);
	
		IVariable var3 = Elements.filterFirst("o3", IVariable.class, type.getEnclosedElements());
		IAnnotation annotation3 = Elements.filterFirst(IAnnotation.class, var3.getEnclosedElements());
		value3 = annotation3.getDefaultValue();
		assertNotNull(value3);
		
		IVariable var4 = Elements.filterFirst("o4", IVariable.class, type.getEnclosedElements());
		IAnnotation annotation4 = Elements.filterFirst(IAnnotation.class, var4.getEnclosedElements());
		value4 = annotation4.getDefaultValue();
		assertNotNull(value4);
		
		transformer = new QuantifierTransformer();
	}
	
	@Test
	public void testTranslate_3() {
		transformer.translate(value3, null);
		String code = Elements.getValue(value3, String.class);
		assertNotNull(code);
		assertEquals("boolean _ForAll0=true; for(Object o : o4) {_ForAll0 &= o.toString() != null;}boolean _Exists1=false; for(Object o : o4) {_Exists1 |= o.toString() != null;} RETURN_STAT _ForAll0 && _Exists1", code);
	
	}
	
	@Test
	public void testTranslate_4() {
		transformer.translate(value4, null);
		String code = Elements.getValue(value4, String.class);
		assertNotNull(code);
		assertEquals("boolean _ForAll0=true; for(Object o : o4) {_ForAll0 &= o.toString() != null;} RETURN_STAT _ForAll0", code);
	
	}
}

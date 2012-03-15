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
/**
 * 
 */
package jass.modern.core.compile.desugar;

import static jass.modern.core.compile.desugar.DesugaringLevel2PatternHelper.TARGET;
import static jass.modern.core.compile.desugar.DesugaringLevel2PatternHelper.insertValues;
import static jass.modern.core.compile.desugar.DesugaringLevel2PatternHelper.replaceTarget;
import static jass.modern.core.compile.desugar.DesugaringLevel2PatternHelper.translate;
import static jass.modern.core.compile.desugar.DesugaringLevel2PatternHelper.translateLength;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jass.modern.Length;
import jass.modern.NonNull;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.Annotation;
import jass.modern.core.model.impl.AnnotationValue;
import jass.modern.core.model.impl.Variable;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

/**
 * @author riejo
 *
 */
public class DesugaringLevel2PatternHelperTest {
	
	String patternLength;
	String patternNotNull;
	
	IAnnotation annotation;
	
	IVariable varArray;
	IVariable varString;
	IVariable varLinkedList;
	
	@Before
	public void setUp() throws Exception {
		patternLength = Length.class.getAnnotation(Level2Desugarable.class).pattern();
		patternNotNull = NonNull.class.getAnnotation(Level2Desugarable.class).pattern();
		
		annotation = new Annotation("jass.modern.Length");
		new AnnotationValue(annotation, "value", 12);
		
		varArray = new Variable("foo", "java.util.Timer[]");
		varString = new Variable("foo", "java.lang.String");
		varLinkedList = new Variable("foo", LinkedList.class.getName());
	}

	@Test
	public void testReplaceTarget() {
		String pattern = patternNotNull;
		assertTrue(pattern.startsWith("(java.lang.Object) " + TARGET));
		
		pattern = replaceTarget(pattern, varArray, null, null);
		assertTrue(pattern.startsWith("(java.lang.Object) " + "foo"));
	}

	@Test
	public void testInsertValues() {
		assertTrue(patternLength.endsWith("@ValueOf(value)"));
		
		patternLength = insertValues(patternLength, annotation, null);
		assertTrue(patternLength.endsWith("12"));
	}

	@Test
	public void testTranslateLengthArray() {
		patternLength = replaceTarget(patternLength, varArray, null, null);
		patternLength = translateLength(patternLength, varArray, null, null);
		assertTrue(patternLength.startsWith("foo.length == "));
	}

	@Test
	public void testTranslateLengthString() {
		patternLength = replaceTarget(patternLength, varArray, null, null);
		patternLength = translateLength(patternLength, varString, null, null);
		assertTrue(patternLength.startsWith("foo.length() == "));
	}
	
	@Test
	public void testTranslateLengthCollection() {
		patternLength = replaceTarget(patternLength, varArray, null, null);
		patternLength = translateLength(patternLength, varLinkedList, null, null);
		assertTrue(patternLength.startsWith("foo.size() == "));
	}
	
	@Test
	public void testTranslate() {
		String pattern = translate(varString, annotation, null);
		assertEquals("foo.length() == 12", pattern);
	}
}

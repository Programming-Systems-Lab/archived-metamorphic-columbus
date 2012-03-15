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
import jass.modern.core.compile.transform.QuantifierTransformer.QuantifierExpressionParser;
import jass.modern.core.compile.transform.QuantifierTransformer.QuantifierTextEdit;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

public class QuantifierExpressionParserTest {

	QuantifierExpressionParser parser = new QuantifierExpressionParser();
	
	@Before
	public void setUp() throws Exception {
		parser.reset();
	}

	@Test
	public void testParse() throws ParseException {
		parser.parse("bal.foo() && @ForAll(Object o : fObject ; false && true)"); 
		QuantifierTextEdit edit = (QuantifierTextEdit) parser.getNext();
		
		assertEquals("Object o", edit.declaration);
		assertEquals("fObject", edit.expression);
		assertEquals("false && true", edit.assertion);
		assertEquals(13, edit.start);
		assertEquals(43, edit.len);
	}

	@Test
	public void testParseMulti() throws ParseException {
		parser.parse("@ForAll(Object o:fObject;true) && false ||Ê@Exists(Number n:numbers;n == 2)");
		QuantifierTextEdit edit = (QuantifierTextEdit) parser.getNext();
		assertEquals("Object o", edit.declaration);
		assertEquals("fObject", edit.expression);
		assertEquals("true", edit.assertion);
		assertEquals(0, edit.start);
		assertEquals(30, edit.len);
		
		edit = (QuantifierTextEdit) parser.getNext();
		assertEquals("Number n", edit.declaration);
		assertEquals("numbers", edit.expression);
		assertEquals("n == 2", edit.assertion);
		assertEquals(43, edit.start);
		assertEquals(32, edit.len);
	}
}

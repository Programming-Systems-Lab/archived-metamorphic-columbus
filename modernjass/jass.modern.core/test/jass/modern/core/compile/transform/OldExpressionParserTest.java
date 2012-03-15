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
import jass.modern.core.compile.transform.OldTransformer.OldExpression;
import jass.modern.core.compile.transform.OldTransformer.OldExpressionParser;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

public class OldExpressionParserTest {

	OldExpressionParser fParser;
	
	@Before
	public void setUp() throws Exception {
		fParser = new OldExpressionParser();
	}
	
	@Test
	public void testParse1() throws ParseException {
		fParser.parse("@Old(accounts).has(a)");
		assertEquals(1, fParser.getExpressionCount());
		
		OldExpression old = (OldExpression) fParser.getNext();
		assertEquals("accounts", old.expression);
		assertEquals(null, old.hint);
		assertEquals(0, old.start);
		assertEquals(14, old.len);
	}
	
	@Test
	public void testParse2() throws ParseException {
		fParser.parse("@Old(accounts) - @Old(abc().str)");
		assertEquals(2, fParser.getExpressionCount());
		
		OldExpression old = (OldExpression) fParser.getNext();
		assertEquals("accounts", old.expression);
		
		old = (OldExpression) fParser.getNext();
		assertEquals("abc().str", old.expression);
	}

	@Test
	public void testParse3() throws ParseException {
		fParser.parse("@Old(abs, int)");
		OldExpression old = (OldExpression) fParser.getNext();
		assertEquals("abs", old.expression);
		assertEquals("int", old.hint);
	}
	
	@Test
	public void testParse4() throws ParseException {
		StringBuilder builder = new StringBuilder("@Old(abs) < 3 && @Old(false)");
		fParser.parse(builder.toString());
		assertEquals(2, fParser.getExpressionCount());
		
		OldExpression old = (OldExpression) fParser.getNext();
		assertEquals(0, old.start);
		assertEquals(9, old.len);

		fParser.insert(old, builder, "foo");
		
		OldExpression old2 = (OldExpression) fParser.getNext();
//		assertEquals(17, old2.start);
//		assertEquals(11, old2.len);
		
		fParser.insert(old2, builder, "hello");
		
		assertEquals("foo < 3 && hello", builder.toString());
	}
}

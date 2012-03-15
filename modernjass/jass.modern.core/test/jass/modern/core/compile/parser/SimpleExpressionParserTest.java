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
package jass.modern.core.compile.parser;

import static org.junit.Assert.assertEquals;
import jass.modern.core.compile.parser.IElementReference.Type;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SimpleExpressionParserTest {

	static final String expression1 = "value() == this.abc.toString()";
	static final String expression2 = "m() == foo.bar()";
	static final String expression3 = "(n() == bar.n().foo) && foo";
	static final String expression4 = "o() == abc.def.ghi.jkl(this.abc)";
	static final String expression5 = "this.id.equals(abc.def.id)";
	static final String expression6 = "(3 - 4) + foo";
	
	SimpleExpressionParser parser;
	
	@Before
	public void setUp() throws Exception {
		parser = new SimpleExpressionParser();
	}

	@Test
	public void testExpression1() {
		List<IElementReference> list = parser.parse(expression1);
		assertEquals(3, list.size());
		
		assertEquals("value", list.get(0).getName());
		assertEquals(0, list.get(0).getParameterCount());
		assertEquals(Type.METHOD_INVOCATION, list.get(0).getType());
		
		assertEquals("abc", list.get(1).getName());
		assertEquals(Type.FIELD_ACCESS, list.get(1).getType());
		
		assertEquals("toString", list.get(2).getName());
		assertEquals(Type.METHOD_INVOCATION, list.get(2).getType());
	}
	
	@Test
	public void testExpression2() {
		List<IElementReference> list = parser.parse(expression2);
		
		assertEquals(3, list.size());
		assertEquals("m", list.get(0).getName());
		assertEquals("foo", list.get(1).getName());
	}

	@Test
	public void testExpression3() {
		List<IElementReference> list;
		list = parser.parse(expression3);
		assertEquals(5, list.size());
		assertEquals("n", list.get(0).getName());
		assertEquals("bar", list.get(1).getName());
		
		assertEquals("n", list.get(2).getName());
		assertEquals(Type.METHOD_INVOCATION, list.get(2).getType());
	}

	@Test
	public void testExpression4() {
		List<IElementReference> list = parser.parse(expression4);
		
		assertEquals(6, list.size());
		assertEquals("o", list.get(0).getName());
		assertEquals("abc", list.get(1).getName());
		assertEquals("def", list.get(2).getName());
		assertEquals("ghi", list.get(3).getName());
		assertEquals("jkl", list.get(4).getName());
		assertEquals("abc", list.get(5).getName());
	}

	@Test
	public void testExpression5() {
		List<IElementReference> list = parser.parse(expression5);
		
		assertEquals(5, list.size());
		
		assertEquals(Type.FIELD_ACCESS, list.get(0).getType());
		assertEquals(Type.UNKNOWN, list.get(4).getType());
		assertEquals(list.get(0).getName(), list.get(4).getName());
	}
	
	@Test
	public void testExpression6() {
		List<IElementReference> list = parser.parse(expression6);
		assertEquals(3, list.size());
	}
}

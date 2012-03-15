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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleExpressionParser {
	
	private static final List<String> OUTS = Arrays.asList(
			"true", "false", "instanceof", "this", 
			"==", "!=", "<", ">", "<=", ">=", 
			"+", "+=", "-", "-=", "&&", "||", "&", "|", "&=", "|=");
	
	private static final Pattern PATTERN = Pattern.compile("\\W*([_\\$a-zA-Z]\\w*)\\((.*)\\)\\W*"); // matching method calls _m(???)
	
	public List<IElementReference> parse(String expression) {
		List<IElementReference> references = new LinkedList<IElementReference>();

		if(expression == null)
			return references;
		
		return _parse(expression, references);
	}

	private List<IElementReference> _parse(String expression, List<IElementReference> references) {
		
		String[] sections = expression.split("(\\.|\\s)");	// split expression by dots and whitspaces
		for (int i = 0; i < sections.length; i++) {
			String section = sections[i].trim();
			boolean _this = i > 0 && sections[i-1].equals("this");
			
			// (1) skip keywords
			if(OUTS.contains(section))
				continue;
			
			if(section.startsWith("("))
				section = section.substring(1);
			
			if(section.endsWith(")") && parentCount(section) != 0)
				section = section.substring(0, section.length() - 1);
			
			int loops = 0;
			while(parentCount(section) != 0) {
				loops += 1;
				if(i + loops >= sections.length)
					break;
				
				String nextSection = sections[i + loops];
				
				section = section + computeSpace(section, nextSection, expression) + nextSection;
			}
			i += loops;
			
			// (2) check for method calls
			Matcher m = PATTERN.matcher(section);
			if(m.matches()) {
				String methodName = m.group(1);
				String methodParam = m.group(2); //computeParams(m.end(1), expression);
				int paramCount = methodParam.length() == 0 ? 0 : methodParam.split("\\,").length;
				
				// (2.1) add method reference
				references.add(new ElementReference(methodName, 
						IElementReference.Type.METHOD_INVOCATION, paramCount));
				
				// (2.2) check method parameters
				if(methodParam.length() > 0) {
					_parse(methodParam, references);
				}
				
			} else if(_this) {
				references.add(new ElementReference(section, IElementReference.Type.FIELD_ACCESS));
				
			} else {
				references.add(new ElementReference(section, IElementReference.Type.UNKNOWN));
			}
		}
		
		return references;
	}

	private String computeSpace(String section, String nextSection,
			String expression) {
		
		int len = section.length();
		int index = expression.indexOf(section, 0);
		int indexNext = expression.indexOf(nextSection, index + len);
		
		char selection = expression.charAt(indexNext - 1);
		return String.valueOf(selection);
	}

	private int parentCount(String section) {
		int c = 0;
		for(int i = 0; i<section.length(); i++) {
			switch(section.charAt(i)) {
			case '(':	
				c += 1;
				break;
				
			case ')':
				c -= 1;
				break;
			}
		}
		
		return c;
	}
	
}

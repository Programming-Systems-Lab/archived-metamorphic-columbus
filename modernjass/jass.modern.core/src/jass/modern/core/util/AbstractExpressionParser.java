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
package jass.modern.core.util;

import java.text.ParseException;
import java.util.LinkedList;

public abstract class AbstractExpressionParser {

	public static class AbstractTextEdit {
		public int start;
		public int len;
	}
	
	protected final LinkedList<AbstractTextEdit> fExpressions = new LinkedList<AbstractTextEdit>(); 
	
	public abstract void parse(String str) throws ParseException;
	
	
	public int getExpressionCount() {
		return fExpressions.size();
	}
	
	public AbstractTextEdit getNext() {
		if(fExpressions.isEmpty())
			return null;
		
		return fExpressions.removeLast();
	}
	
	public void reset() {
		fExpressions.clear();
	}

	public void insert(AbstractTextEdit edit, StringBuilder code, String replacement) {
		code.replace(edit.start, edit.start + edit.len, replacement);
		int offset = replacement.length() - edit.len;
		
		for (AbstractTextEdit expression : fExpressions) {
			expression.start += offset;
		}
	}
}

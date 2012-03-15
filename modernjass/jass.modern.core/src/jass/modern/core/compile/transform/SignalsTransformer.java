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

import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.util.Elements;

import java.util.regex.Matcher;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class SignalsTransformer implements IAnnotationValueTransformer {

	private final static String SIGNAL_VAR_NAME = "_Signal";
	
	private final static String SIGNALS_KEY = "@Signal";
	
	@Override
	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {
	
		String code = Elements.getValue(value, String.class);
		
		if(code == null)
			return;
		
		code = code.replaceAll(SIGNALS_KEY, Matcher.quoteReplacement(SIGNAL_VAR_NAME));
		value.setValue(code);
	}

}

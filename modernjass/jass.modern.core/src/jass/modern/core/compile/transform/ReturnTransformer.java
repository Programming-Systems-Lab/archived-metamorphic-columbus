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

import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.util.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

/**
 * Transforms the <code>@Return</code> and <code>@Result</code>
 * specification expression into a valid variable name which
 * holds the return value of a method.
 * 
 * @author riejo
 */
public class ReturnTransformer implements IAnnotationValueTransformer {
	
	public static final String RETURN_VAR_NAME = "_Return";
	
	private static final Pattern PATTERN = Pattern.compile("(@Return|@Result)");
	
	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {

		String code = Elements.getValue(value, String.class);
		Matcher m = PATTERN.matcher(code);
		
		IExecutable target = Elements.getParent(IExecutable.class, value);
		boolean noReturn = !target.isConstructor() && target.getReturnType().equals("void");
		
		if(noReturn && m.find()) {
			diagnostics.report(new ContractDiagnostic(value, 
					m.group() + " can not be used because return type of " +
					target.getSimpleName() + " is void", Kind.ERROR));
			
		} else {
			code = m.replaceAll(RETURN_VAR_NAME);
			value.setValue(code);
		}
	}
}

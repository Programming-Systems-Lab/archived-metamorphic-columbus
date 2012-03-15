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
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.impl.ContractExecutableElement;
import jass.modern.core.util.AbstractExpressionParser;
import jass.modern.core.util.Elements;
import jass.modern.core.util.AbstractExpressionParser.AbstractTextEdit;

import java.text.ParseException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * An {@link IAnnotationValueTransformer annotation value transformer} which
 * translate <code>@ForAll</code> and <code>@Exists</code> expression into
 * equivalent Java source code. <br /> <br />
 * 
 * The grammar for <code>@ForAll</code> and <code>@Exists</code> expression
 * is similar to Java for loops (normal, enhanced). For instance, one might
 * write
 * <pre>@Exists(Object o : obj; o == null)</pre>
 * and after the transformation
 * <pre>
 * boolean Exists0 = false;
 * for(Object o : obj){
 * 	Exists0 |= o == null;
 * }
 * </pre>
 * is used.
 * <br />
 * 
 * <em>Note:</em> This transformer does not validate the expression. It is only
 * 	a set of pattern-based string operations and not a parser. Errors will be 
 * 	detected by the {@link ContractJavaCompiler compiler}.
 * 
 *  <br />
 * <em>Note:</em> Quantifier expression are evaluated to return as soon as possible. 
 * This means that a exists operation will return <code>true</code> after as soon 
 * as the value changes to <code>true</code>. The same for the forall operations, which
 * returns as soon as the value will return <code>false</code>.
 * 
 * @author riejo
 */
public class QuantifierTransformer implements IAnnotationValueTransformer {

	public static class QuantifierTextEdit extends AbstractTextEdit {
		boolean forall;
		String declaration;
		String expression;
		String assertion;
	}
	
	public static class QuantifierExpressionParser extends AbstractExpressionParser {

		private static final String FORALL = "@ForAll(";
		private static final String EXISTS = "@Exists(";
		
		@Override
		public void parse(String str) throws ParseException {
			int offset = 0;
			int oldOffset = 0;
			while(internalParse(offset, str)) {
				
				oldOffset = offset;
				offset = fExpressions.peek().start + fExpressions.peek().len;
				str = str.substring(offset - oldOffset);
			}
		}
		
		private boolean internalParse(int offset, String str) throws ParseException {
			
			QuantifierTextEdit textEdit = new QuantifierTextEdit();

			int index = str.indexOf(FORALL);
			if(index != -1) {
				textEdit.forall = true;
				textEdit.start = offset + index;
				
			} else {
				index = str.indexOf(EXISTS);
				textEdit.forall = false;
				textEdit.start = offset + index;
			}
			
			if(index == -1) {
				return false;
			}
			
			index += textEdit.forall ? FORALL.length() : EXISTS.length();
			int[] declaration = new int[] {index, -1};
			int[] expression = new int[] { -1, -1};
			int[] assertion = new int[] { -1, -1};
			
			int open = 1;
			while(open != 0 && index < str.length()) {
				char tmp = str.charAt(index);
				switch (tmp) {
				case ')':
					open -= 1;
					break;
					
				case '(':
					open += 1;
					break;
					
				case ':':
					declaration[1] = index;
					expression[0] = index +1;
					break;
					
				case ';':
					expression[1] = index;
					assertion[0] = index +1;
					break;
				}
				
				index += 1;
			}
			assertion[1] = index-1;
			
			if(open != 0)	throw new ParseException("Unclosed open bracket", index);
			if(declaration[0] * declaration[1] < 0) throw new ParseException("Invalid declaration", index);
			if(expression[0] * expression[1] < 0)	throw new ParseException("Invalid expression", index);
			if(assertion[0] * assertion[1] < 0)		throw new ParseException("Invalid assertion", index); 
			
			textEdit.len = (index + offset) - textEdit.start;
			textEdit.declaration = str.substring(declaration[0], declaration[1]).trim();
			textEdit.expression = str.substring(expression[0], expression[1]).trim();
			textEdit.assertion = str.substring(assertion[0], assertion[1]).trim();
			
			fExpressions.push(textEdit);
			return true;
		}
	}
	

	private QuantifierExpressionParser parser = new QuantifierExpressionParser();
	
	@Override
	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {

		String code = Elements.getValue(value, String.class);
		if(code == null)
			return;
		
		try {
			parser.reset();
			parser.parse(code);
			
		} catch(ParseException e) {
			diagnostics.report(new ContractDiagnostic(value, e.getMessage(), 
					Diagnostic.Kind.ERROR));
			return;
		}
		
		if(parser.getExpressionCount() == 0)
			return;
		
		StringBuilder buffer = new StringBuilder(code);
		StringBuilder returnStatement = new StringBuilder();
		
		int n = 0;
		QuantifierTextEdit edit = null;
		while((edit = (QuantifierTextEdit) parser.getNext()) != null) {
			String replacement = print(n, edit);
			returnStatement.append(replacement);
			
			parser.insert(edit, buffer, variableName(n, edit));
			n +=1;
		}
	
		value.setValue(returnStatement.toString() + 
				" " +ContractExecutableElement.RETURN_STAT+" " + 
				buffer.toString());
	}
	
	private String print(int n, QuantifierTextEdit edit) {
		StringBuilder builder = new StringBuilder();
		String varName = variableName(n, edit);
		String operator = edit.forall ? " &= " : " |= ";
		
		builder.append("boolean " + varName + "=" + edit.forall + "; ");
		builder.append("for(" + edit.declaration + " : " + edit.expression + ") {");
		builder.append(varName + operator + edit.assertion + ";");
		builder.append("if("+(edit.forall ? "!" : "" ) + varName+ ") break;");
		builder.append("}");
		
		return builder.toString();
	}

	private String variableName(int n, QuantifierTextEdit edit) {
		return (edit.forall ? "_ForAll" : "_Exists") + n;
	}
}

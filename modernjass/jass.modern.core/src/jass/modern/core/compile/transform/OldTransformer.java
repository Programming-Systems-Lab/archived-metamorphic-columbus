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
import jass.modern.core.compile.creation.Helper;
import jass.modern.core.compile.creation.ModelVariableHelper;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.ContractExecutableElement;
import jass.modern.core.util.AbstractExpressionParser;
import jass.modern.core.util.Configuration;
import jass.modern.core.util.Elements;
import jass.modern.core.util.JavaUtil;
import jass.modern.core.util.Primitive;
import jass.modern.core.util.AbstractExpressionParser.AbstractTextEdit;
import jass.modern.core.util.Configuration.Issue;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

/**
 * A translator for the specification expression <code>@Old</code>.
 * 
 * The specification expression is defined by this grammer:
 * <pre>
 * OLD        := "@Old(" + EXPRESSION + TYPE_HINT? + ")"
 * EXPRESSION := Java expression, see JLS chapter 15
 * TYPE_HINT  := "," + TYPE
 * TYPE       := primitive or reference type, JLS chapter 4
 * </pre>
 * Further restrictions:
 * <ul>
 * <li><code>MEMBER</code> must be a valid Java identifier.
 * <li><code>TYPE</code> must be a valid Java identifier or a primitive type.
 * </ul>
 * 
 * <b>Type hint example:</b>
 * <pre>
 * class CU {
 *   int returnsAnInt(String a){ ...
 *   String returnsAnInt(Object o){...
 * }
 * 
 * &#064;Post("&#064;Old(returnsAnInt((String) null), int) == 3")
 * void m(){
 * 	//...
 * }
 * </pre>
 * Type hints are required when Modern Jass fails to infer the type
 * of a member. This will happen with method overloading. <em>Note:</em>
 * The type hint is trusted without further checks.
 * 
 * @author riejo
 */
public class OldTransformer implements IAnnotationValueTransformer {
	
	public static final class OldExpression extends AbstractTextEdit {
		String expression;
		String hint;
	}
	
	public static final class OldExpressionParser extends AbstractExpressionParser {
		
		private static final String OLD = "@Old(";
	
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
			
			int index = str.indexOf(OLD);
			if(index == -1)
				return false;
			
			OldExpression old = new OldExpression();

			index += OLD.length();
			int start1 = index;
			int comma = 0;
			
			int open = 1;
			while(open != 0 && index < str.length()) {
				char tmp = str.charAt(index);
				
				switch(tmp) {
				case '(':
					open += 1;
					break;
				case ')':
					open -= 1;
					break;
				case ',':
					comma = index;
					break;
				}
				
				index += 1;
			}
			
			if(open != 0) {
				throw new ParseException("Unclosed bracket in expression " + str, index);
			}
			
			old.start = offset + start1 - OLD.length();
			old.len =  (index - start1) + OLD.length();
			
			if(comma != 0) {
				old.expression = str.substring(start1, comma).trim();
				old.hint = str.substring(comma + 1, index - 1).trim();
				
			} else {
				old.expression = str.substring(start1, index - 1).trim();
			}
			
			fExpressions.push(old);
			return true;
		}
	}
	
	public static final String OLD = "old";
	
	private final OldExpressionParser parser = new OldExpressionParser();
	
	private final ModelVariableHelper fModelVariablesHelper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	
	@Override
	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {
		
		StringBuilder code = new StringBuilder(Elements.getValue(value, String.class));
		if(code == null)
			return;
		
		IExecutable method = Elements.getParent(IExecutable.class, value);
			
		parser.reset();
		try {
			parser.parse(code.toString());
			
		} catch (ParseException e) {
			diagnostics.report(new ContractDiagnostic(value, 
					e.getMessage(), Kind.ERROR));
			return;
		}
		
		OldExpression old;
		while((old = (OldExpression) parser.getNext()) != null) {
			String member = old.expression;
			String type = old.hint;
			
			String memberInternalName = member;
			int index = member.lastIndexOf(ModelVariableTransformer.SUFFIX);
			member = index != -1 ? member.substring(0, index) : member;
			
			if(type == null) {
				type = inferType(method, member);
			}
			
			if(type == null) {
				diagnostics.report(new ContractDiagnostic(value, 
						"Could not resolve the type of " + member, 
						Kind.ERROR));
				
				diagnostics.report(new ContractDiagnostic(value, "You can give " +
						"a type hint by writing @Old(" + member + ", TYPE)", 
						Kind.WARNING));
				
				continue;
			}
			
			Configuration.Type issue = Configuration.getValue(Issue.OLD_OF_NONE_PURE);
			if(issue != Configuration.Type.IGNORE && !JavaUtil.isPure(type)) {
				diagnostics.report(new ContractDiagnostic(value, type + " is a " + 
					"reference type but not marked as @Pure", issue.toDiagnosticsKind()));
			}
			
			parser.insert(old, code, "((" + box(type) + ") _Context.old(\""+escape(memberInternalName)+"\"))");
			
			createOldExecutable(method, memberInternalName);
		}
		
		value.setValue(code.toString());
	}

	/**
	 * Creates a new method which represents the value of 
	 * a <code>@Old</code> expression.
	 * 
	 * @param method
	 * @param target
	 */
	public void createOldExecutable(IExecutable method, String target) {
		
		// (1) escape name
		String nameSuffix = escape(target);
		
		// (2) create contract method
		IContractExecutable tmp = new ContractExecutableElement(method.getSimpleName() + 
				Helper.SEPARATOR + OLD + Helper.SEPARATOR + nameSuffix ,
				"java.lang.Object", target);
		
		method.getEnclosingElement().removeEnclosedElement(tmp);
		method.getEnclosingElement().addEnclosedElement(tmp);
	}

	/**
	 * Starting at <code>element</code>, a parameter, field,
	 * or method is searched, which name equals the parameter 
	 * <code>name</code>.<br />
	 * <em>Note:</em> This method is not able to infer the
	 * type of a overloaded method. One should use a type hint 
	 * instead.
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	private String inferType(IExecutable element, String name) {

		// (1) check all parameters
		List<IVariable> parameters = element.getParameters();
		for (IVariable parameter : parameters) {
			if(parameter.getSimpleName().equals(name)) {
				return parameter.getType();
			}
		}
		
		// (2) check all members (declared & inherited)
		IType type = Elements.getParent(IType.class, element);
		List<IElement> members = Elements.getAllMembers(type);
		boolean executable = name.endsWith(")");
		
		if(executable) {
			return inferExecutableType(name, type, members);
		
		} else {
			/*
			 * does include model variables
			 */
			return inferFieldType(name, type, members);
		}
	}

	private String inferExecutableType(String name, IType type,
			List<IElement> members) {
		
		int index = name.indexOf('(');
		name = name.substring(0, index);
		
		List<IExecutable> candidates = Elements.filter(name, IExecutable.class, members);
		if(candidates.isEmpty()) {
			return null;
		
		} else if(candidates.size() == 1) {
			return candidates.get(0).getReturnType();
		
		} else {
			return null;
		}
	}

	private String inferFieldType(String name, IType type, List<IElement> members) {
		
		// (1) check fields
		List<IVariable> candidates = Elements.filter(name, IVariable.class, members);
		if(candidates.size() == 1) {
			return candidates.get(0).getType();
		} 

		// (2) check model variables
		Map<String, Class<?>> modelVariables = fModelVariablesHelper.getAllModelVariables2(type);
		Class<?> tmp = modelVariables.get(name);
		if(tmp != null)
			return tmp.getName();
		
		return null;
	}

	/**
	 * Boxes primitive types into their wrapper object.
	 * E.g, calling
	 * <pre>box("short")</pre>
	 * will return <code>java.lang.Character</code>.
	 * <br />
	 * If the passed type is not a primitive type, 
	 * nothing happens.
	 * 
	 * @param type
	 * @return 
	 */
	private String box(String type) {
		
		Primitive t = Primitive.parseString(type);
		if(t != null)
			return t.asWrapper();
			
		return type;
	}

	private String escape(String target) {
		String nameSuffix = target;
		nameSuffix = nameSuffix.replace('(', 'L');
		nameSuffix = nameSuffix.replace(')', 'R');
		return nameSuffix;
	}
}

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
package jass.modern.core.compile.desugar;

import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;
import jass.modern.core.util.Primitive;
import jass.modern.core.util.Primitive.Array;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

public class DesugaringLevel2PatternHelper {
	
	/**
	 * The constant for the &#64;Target expression.
	 */
	public static final String TARGET = "@Target";
	
	/**
	 * The constant for the &#064;Length expression.
	 */
	public static final String LENGTH = "@Length";
	
	/**
	 * The regular expression to parse the '@ValueOf(MEMBER)'
	 * expressions
	 */
	public static final Pattern REGEX_VALUE = Pattern.compile(
			"@ValueOf\\((\\$?\\w+?)\\)");
	
	
	static Class<?> type(IVariable var){
		
		// (1) primitive type ala int, long, double...
		Primitive p = Primitive.parseString(var.getType());
		if(p != null)
			return p.getType();
		
		// (2) generic type defined by its method
		IExecutable method = Elements.getParent(IExecutable.class, var);
		if(method != null && method.getGenericSignature().contains(var.getType()))
			return Object.class;
		
		// (3) generic type 
		IType parent = Elements.getParent(IType.class, var);
		if(parent != null && parent.getGenericSignature().contains(var.getType()))
			return Object.class;
		
		// (4) infer type
		Class<?> type = ContractJavaCompiler.getInstance().forName(var.getType());
		return type;
	}
	
	static String replaceTarget(String pattern, IVariable var, IAnnotation annotation, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		Class<?>[] targets = Contracts.getTypes(annotation);
		if(targets != null && ! (targets.length == 1 && targets[0].equals(Void.class))) {
			Class<?> type = type(var);
			boolean assignable = false;
			for (Class<?> targetType : targets) {
				if(type == null)
					continue;
				
				assignable |= targetType.isAssignableFrom(type);
			}
			
			if(!assignable) {
				
				diagnostics.report(new ContractDiagnostic(annotation, "The @" + 
					annotation.getSimpleName() + "-annotation can not be " +
					"applied to the type " + var.getType(), Kind.ERROR));
				
				diagnostics.report(new ContractDiagnostic(annotation, "The @" + 
					annotation.getSimpleName() + "-annotation can be applied" +
					"to the types and subtypes: " + Arrays.toString(targets), 
					Kind.NOTE));
				
				return pattern;
			}
		}
		
		return pattern.replaceAll(TARGET, Matcher.quoteReplacement(var.getSimpleName()));
	}
	
	static String insertValues(String pattern, IAnnotation annotation, 
			DiagnosticCollector<JavaFileObject> diagnostics) {
		
		Matcher matcher = REGEX_VALUE.matcher(pattern);
		while(matcher.find()) {
			String attributeName = matcher.group(1);
			Object value = Elements.getValue(annotation, attributeName, Object.class);
			
			if(value == null) {
			
				IAnnotationValue tmp = annotation.getValue(attributeName);
				diagnostics.report(new ContractDiagnostic(tmp, "The attribute " + 
						attributeName + " from " + annotation.getSimpleName() + " could not" +
						"be resolved.", Kind.ERROR));
				
				continue;
			}
			
			pattern = pattern.replaceFirst(REGEX_VALUE.pattern(), value.toString());
		}
		
		return pattern;
	}
	
	static String translateLength(String pattern, IVariable variable, IAnnotation annotation,
			DiagnosticCollector<JavaFileObject> diagnostics) {

		String lengthFunc = null;
		Class<?> type = type(variable);

		if(type.equals(Array.class)) {
			lengthFunc = "length";

		} else if(type.equals(String.class)){
			lengthFunc = "length()";
		
		} else if(Collection.class.isAssignableFrom(type)) {
			lengthFunc = "size()";
			
		} else {
			return pattern;
		}
		
		return pattern.replaceAll(LENGTH, Matcher.quoteReplacement(lengthFunc));
	}
	
	public static String translate(IVariable variable, IAnnotation annotation, DiagnosticCollector<JavaFileObject> diagnostics) {
		
		String pattern = Contracts.getPattern(annotation);
		
		if(pattern == null)
			return null;
		
		// (1) replace the '@Target'-placeholder with the variable name
		pattern = replaceTarget(pattern, variable, annotation, diagnostics);
		
		// (2) insert the value of the annotation attributes, e.g. valueOf(to)
		pattern = insertValues(pattern, annotation, diagnostics);
		
		// (3) resolve the length function and insert it
		if(pattern.contains(LENGTH)) {
			pattern = translateLength(pattern, variable, annotation, diagnostics);
		}
		
		return pattern;
	}
}

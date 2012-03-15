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
package jass.modern.core.compile.creation;

import jass.modern.Visibility;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.parser.IElementReference;
import jass.modern.core.compile.parser.SimpleExpressionParser;
import jass.modern.core.compile.parser.IElementReference.Type;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.Annotation;
import jass.modern.core.model.impl.AnnotationValue;
import jass.modern.core.util.Configuration;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;
import jass.modern.core.util.Configuration.Issue;
import jass.modern.meta.ContractInfo;

import java.util.Iterator;
import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

public class Helper {
	
	public final static String SEPARATOR = "$";

	public final static String PRE = "pre";
	
	public final static String POST = "post";
	
	public final static String SIGANLS = "signals";
	
	public final static String INVAR = "invar";

	public final static String MODEL = "model";
	
	private final static SimpleExpressionParser fParser = new SimpleExpressionParser();
	
	public static void addMetaInfo(IExecutable contractElement,	IAnnotation annotation, 
			String key, String code) {
		
		IAnnotation metaAnnotation = new Annotation(ContractInfo.class.getName());
		String msg = Elements.getValue(annotation, key, String.class);
		new AnnotationValue(metaAnnotation, "message", msg != null ? escape(msg) : "");
		new AnnotationValue(metaAnnotation, "code", escape(code));
		
		contractElement.addEnclosedElement(metaAnnotation);
	}

	private static String escape(String str) {
		if(str == null)
			return null;
		
		return str.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/**
	 * Validates the visibility of a contract. This method works in 
	 * two steps
	 * <ol>
	 * <li>The contract of the annotation is validated against its target.
	 * 	E.g this means, that the contract can not be 'more' visible than
	 * 	its target. (No <code>public</code> contracts for <code>private</code>
	 * 	methods, ...)
	 * 
	 * <li>All elements which are referenced in a contract must not be
	 * 	less visible than the contract it self. This means, that a 
	 *  <code>public</code> contract can not use a <code>private</code>
	 *  member.
	 * </ol>
	 * 
	 * Example for illegal visibility arragements
	 * <ol>
	 * <li>Contract is less restrictive than its target.
	 *  <pre>
	 *  &#064;SpecCase(pre="a != null", visibility = PUBLIC)
	 *  private void m(Object a){ //...
	 *  </pre>
	 *  
	 * <li>Contract refers to invisible members.
	 *  <pre>
	 *  class CU {
	 *   private int a;
	 *  
	 *   &#064;SpecCase(pre = "this.a > a")
	 *   public void m(int a){ //...
	 *  }
	 * </ol>
	 * This method will report all violations to the passes {@link DiagnosticListener}
	 * and abort early.
	 * <br />
	 * <em>Performance note:</em> This method is expensive because parsing of
	 * 	the contract code and resolving all visibile members is required.
	 * @param target
	 * @param diagnostics
	 * @param annotation
	 */
	public static void validateCodeVisibility(IAnnotationValue visibility,/* boolean _static,*/ IAnnotationValue code, 
			IElement target, DiagnosticListener<JavaFileObject> diagnostics) {
		
		Visibility contractVisibility = Contracts.getContractVisibiliy(visibility, target);
		
		// (2) check for invisible references
		String _code = Elements.getValue(code, String.class);
		List<IElementReference> references = fParser.parse(_code);
		
		// (2.1) remove parameters from reference list if target is executable
		if(target instanceof IExecutable) {
			
			List<IVariable> parameters = ((IExecutable) target).getParameters();
			for (IVariable parameter : parameters) {
				for (Iterator<IElementReference> iter = references.iterator(); iter.hasNext(); ) {
					
					IElementReference reference = iter.next();
					if(reference.getType() == Type.UNKNOWN && reference.matches(parameter)) {
						
						iter.remove();
					}
				}
			}
		}
		
		// (2.2) check resolvable references and @Pure-annotation
		IType type = null;
		if (target instanceof IType) {
			type = (IType) target;
		} else {
			type = Elements.getParent(IType.class, target);
		}
		List<IElement> members = Elements.getAllMembers(type);
		for (IElement member : members) {
			for (IElementReference reference : references) {
				
				if(reference.matches(member)) {
					
					// (2.2.1) check visibility
					Visibility tmp = Elements.getVisibility(member);
					Visibility referenceVisibility = Visibility.PUBLIC.compareTo(tmp) < 0 ? 
							tmp : Visibility.PUBLIC;
					
					if(contractVisibility.compareTo(referenceVisibility) < 0) {
						diagnostics.report(new ContractDiagnostic(code, 
						referenceVisibility + " member " + member.getSimpleName() + 
						" is invisible in contract with visibility " + contractVisibility, 
						Kind.ERROR));
						
						return;
					}
					
//					XXX check is done by the Java compiler
//					// (2.2.2) check static or instance
//					if(_static && !member.getModifiers().contains(Modifier.STATIC)) {
//						diagnostics.report(new ContractDiagnostic(code, member  + 
//								" can be referenced from a static context", 
//								Kind.ERROR));
//					}
					
					// (2.2.3) check for @Pure
					Configuration.Type issue = Configuration.getValue(Issue.NON_PURE_USE);
					if(issue != Configuration.Type.IGNORE && 
							reference.getType() == IElementReference.Type.METHOD_INVOCATION ) {
						
						if(Elements.filter("jass.modern.Pure", IAnnotation.class, 
								member.getEnclosedElements()).isEmpty()) {
							
							diagnostics.report(new ContractDiagnostic(code, 
							"Methods referenced in contracts should be marked as " +
							"side-effect-free. See: @jass.modern.Pure-annotation.", 
							issue.toDiagnosticsKind()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Validates that the visibility modifier defined by an contract
	 * does not conflict with the visibility of its target.
	 * 
	 * @param visibility
	 * @param target
	 * @param diagnostics
	 * @return
	 */
	public static boolean validateContractVisibility(IAnnotationValue visibility, IElement target,
			DiagnosticListener<JavaFileObject> diagnostics) {
		
		Visibility targetVisibility = Elements.getVisibility(target);
		Visibility contractVisibility = Contracts.getContractVisibiliy(visibility, target);
		
		if(contractVisibility.compareTo(targetVisibility) < 0) {
			diagnostics.report(new ContractDiagnostic(visibility, 
					"Contract visibility must be equal or less than " + targetVisibility, 
					Kind.ERROR));
			
			return false;
		}
		
		return true;
	}
}

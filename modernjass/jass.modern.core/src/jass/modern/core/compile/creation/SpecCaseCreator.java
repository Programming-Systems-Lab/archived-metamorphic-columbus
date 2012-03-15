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

import static jass.modern.core.compile.creation.Helper.POST;
import static jass.modern.core.compile.creation.Helper.PRE;
import static jass.modern.core.compile.creation.Helper.SEPARATOR;
import static jass.modern.core.compile.creation.Helper.SIGANLS;
import static jass.modern.core.compile.creation.Helper.addMetaInfo;
import static jass.modern.core.compile.creation.Helper.validateCodeVisibility;
import static jass.modern.core.compile.creation.Helper.validateContractVisibility;
import jass.modern.Also;
import jass.modern.SpecCase;
import jass.modern.core.compile.creation.IContractCreator.ContractTypes;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.ContractExecutableElement;
import jass.modern.core.model.impl.Variable;
import jass.modern.core.runtime.ContractContext;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;

import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

@ContractTypes( { Also.class, SpecCase.class })
public class SpecCaseCreator implements IContractCreator {

	private static final String NAME_SPEC = Also.class.getName();
	
	private static final String NAME_CASE = SpecCase.class.getName();
	
	@SuppressWarnings("unchecked")
	@Override
	public void create(IAnnotation annotation, IType parent, DiagnosticListener<JavaFileObject> diagnostics) {
		
		IExecutable element = Elements.getParent(IExecutable.class, annotation);
		if(!element.isConstructor())
			element.addModifier(Modifier.ABSTRACT);
		
		int i = 0;
		if(annotation.getSimpleName().equals(NAME_SPEC)) {

			List<IAnnotation> cases = Elements.getDefaultValue(annotation, List.class);
			for (IAnnotation _case : cases) {
				
				handleCase(parent, element, _case, i, diagnostics);
				i += 1;
			}
			
		} else if(annotation.getSimpleName().equals(NAME_CASE)) {
			
			handleCase(parent, element, annotation, i, diagnostics);
		}
	}
	
	protected void handleCase(IType parent, IExecutable element, IAnnotation _case, 
			int i, DiagnosticListener<JavaFileObject> diagnostics) {
		
		// (1) check that the visibility of this speccase is valid
		IAnnotationValue visibility = _case.getValue("visibility");
 		boolean valid = validateContractVisibility(visibility, element, diagnostics);
 		if(!valid)
 			return;
 		
 		// (2) create contract code for each element of the speccase
		IAnnotationValue pre = _case.getValue("pre");
		IAnnotationValue post = _case.getValue("post");
		IAnnotationValue signalsPost = _case.getValue("signalsPost");
		IAnnotationValue signals = _case.getValue("signals");
		
		// (3) add default values
		if(signalsPost == null && signals == null) {
			signals = Contracts.defaultSignals();
			signalsPost = Contracts.signalsPost("false");
			
		} else if(signalsPost == null && signals != null) {
			signalsPost = Contracts.signalsPost("true");
		}
		
		
		if(pre != null) {
			IExecutable tmp = createPreContract(visibility, pre, element, i, 
					_case, diagnostics);
			parent.addEnclosedElement(tmp);
		}
		if(post != null) {
			IExecutable tmp = createPostContract(visibility, post, element, i, 
					_case, diagnostics);
			parent.addEnclosedElement(tmp);
		}
		if(signals != null && signalsPost != null) {
			IExecutable tmp = createSignalsContract(visibility, signals, signalsPost, 
					element, i, _case, diagnostics);
			parent.addEnclosedElement(tmp);
		}
	}
	
	protected IExecutable createPreContract(IAnnotationValue visibilityValue, IAnnotationValue contract, IExecutable element, int i, 
			IAnnotation annotation, DiagnosticListener<JavaFileObject> diagnostics) {
		
		// (1) create contract element
		String name = element.getSimpleName() + SEPARATOR + PRE + SEPARATOR + i;
		String code = Elements.getValue(annotation, "pre", String.class);
		IExecutable contractElement = newContractElement(element, name, code, contract);
		addMetaInfo(contractElement, annotation, "preMsg", code);
		contractElement.addModifier(Contracts.getContractVisibiliy(
				visibilityValue, element).toModifier());

		// (2) validate contract elements
		validateCodeVisibility(visibilityValue, contract, element, diagnostics);
		
		return contractElement;
	}

	protected IExecutable createPostContract(IAnnotationValue visibilityValue, IAnnotationValue contract, 
			IExecutable element, int i,	IAnnotation annotation, DiagnosticListener<JavaFileObject> diagnostics) {
		
		// (1) create contract element
		String name = element.getSimpleName() + SEPARATOR + POST + SEPARATOR + i;
		String code = Elements.getValue(contract, String.class);
		IVariable returnVar = new Variable("_Return", polishReturnType(element));
		returnVar.addModifier(Modifier.FINAL);
		IVariable contextVar = new Variable("_Context", ContractContext.class.getName());
		contextVar.addModifier(Modifier.FINAL);

		IExecutable contractElement = newContractElement(element, name, code, contract, returnVar, contextVar);
		addMetaInfo(contractElement, annotation, "postMsg", code);
		contractElement.addModifier(Contracts.getContractVisibiliy(
				visibilityValue, element).toModifier());
		
		// (2) validate contract element
		validateCodeVisibility(visibilityValue, contract, element, diagnostics);
		
		return contractElement;
	}
	
	protected IExecutable createSignalsContract(IAnnotationValue visibilityValue, IAnnotationValue signals, 
			IAnnotationValue signalsPost, IExecutable element, int i, 
			IAnnotation annotation, DiagnosticListener<JavaFileObject> diagnostics) {

		// (1) gather all information
		String name = element.getSimpleName() + SEPARATOR + SIGANLS + SEPARATOR + i;
		String signalType = Elements.getValue(signals, Class.class).getName();
		String code = Elements.getValue(signalsPost, String.class);
//		code = code == null ? "true" : code;
		code = "!(_Signal instanceof " + signalType + ") || " + code;
		
		IVariable signalVar = new Variable("_Signal", Throwable.class.getName());
		signalVar.addModifier(Modifier.FINAL);
		IVariable contextVar = new Variable("_Context", ContractContext.class.getName());
		contextVar.addModifier(Modifier.FINAL);
		
		// (2) create contract element
		IExecutable contractElement = newContractElement(element, name, code, 
				signalsPost != null ? signalsPost : signals, signalVar, contextVar);
		addMetaInfo(contractElement, annotation, "signalsMsg", code);
		contractElement.addModifier(Contracts.getContractVisibiliy(
				visibilityValue, element).toModifier());
		
		// (3) validate contract element
		if(signals != null)
			validateCodeVisibility(visibilityValue, signals, element, diagnostics);
		
		return contractElement;
	}
	
	private ContractExecutableElement newContractElement(IExecutable target, String name, String code, 
			IAnnotationValue contract, IVariable...variables) {
		
		ContractExecutableElement tmp = new ContractExecutableElement(name, target, contract, code);
		for (IVariable variable : variables) {
			tmp.addEnclosedElement(0, variable);
		}
		return tmp;
	}

	private String polishReturnType(IExecutable element) {
		if(element.isConstructor())
			return "java.lang.Void";
		
		if(element.getReturnType().equals("void"))
			return "java.lang.Void";
		
		return element.getReturnType();
	}
}

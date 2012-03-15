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

import jass.modern.NonNull;
import jass.modern.Visibility;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.impl.AnnotationValue;
import jass.modern.core.model.impl.Variable;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.ElementScanner;
import jass.modern.core.util.Elements;

import java.lang.annotation.ElementType;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Level <code>2</code> desugaring targets marker annotations
 * like {@link NonNull} which might be placed at method parameters
 * or fields.
 *
 * @author riejo
 */
public class DesugaringLevel2Visitor extends ElementScanner<DiagnosticCollector<JavaFileObject>> {
	
	
	@Override
	public void visit(IAnnotation element, DiagnosticCollector<JavaFileObject> diagnostics) {
		
		if(!Contracts.isLevel2Flyweight(element))
			return;
		
		ElementType target = getTargetElementType(element);
		
		IVariable variable = target != ElementType.METHOD ? 
				(IVariable) element.getEnclosingElement() : 
					new Variable("@Result", ( (IExecutable) element.getEnclosingElement()).getReturnType());
				
		String code = DesugaringLevel2PatternHelper.translate(variable, element, diagnostics);
		switch(target) {
		case FIELD:
			IType type = Elements.getParent(IType.class, element);
			addInvariant(type, code, Contracts.getContractVisibiliy(
					element, element.getEnclosingElement()));
			break;
			
		case METHOD:
			IExecutable method = Elements.getParent(IExecutable.class, element);
			addToPostConditions(method, code);
			break;
			
		case PARAMETER:
			method = Elements.getParent(IExecutable.class, element);
			addToPreConditions(method, code);
			break;
			
		default:
			throw new IllegalStateException();	
		}
		
		element.getEnclosingElement().removeEnclosedElement(element);
	}

	protected ElementType getTargetElementType(IAnnotation element) {
		IElement target = element.getEnclosingElement();
		if(target instanceof IExecutable) {
			return ElementType.METHOD;
			
		} else {
			target = Elements.getParent(IExecutable.class, target);
			if(target != null) {
				return ElementType.PARAMETER;
				
			} else {
				return ElementType.FIELD;
			}
		}
	}

	protected void addInvariant(IType type, String code, Visibility visibility) {
		List<IAnnotation> invariants = Contracts.getInvariants(type);
		IAnnotation invariant = Contracts.newInvariant(code, visibility);
		
		invariants.add(invariant);
		
		Contracts.removeInvariants(type);
		IAnnotation invariantDefinitions = Contracts.newInvariantDefinitionClause(
				invariants.toArray(new IAnnotation[invariants.size()]));
		type.addEnclosedElement(invariantDefinitions);
	}
	
	protected void addToPreConditions(IExecutable method, String code) {
		addToSpecAttribute(method, code, ContractTarget.PRE);
	}

	protected void addToPostConditions(IExecutable method, String code) {
		addToSpecAttribute(method, code, ContractTarget.POST);
	}

	protected void addToSpecAttribute(IExecutable method, String code, ContractTarget target) {

		List<IAnnotation> specs = Contracts.getSpecCases(method);
		if(specs.isEmpty()) {
			IAnnotation spec = Contracts.newSpecCase(null, null, null, null);
			new AnnotationValue(spec, target.toString(), code);
			method.addEnclosedElement(spec);
			
		} else {
		
			for (IAnnotation spec : specs) {
				IAnnotationValue value = spec.getValue(target.toString());
				if(value == null) {
					new AnnotationValue(spec, target.toString(), code);
					
				} else {
					code = code + " && " + Elements.getValue(value, String.class);
					value.setValue(code);
				}
			}
		}
	}
}

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

import static jass.modern.core.compile.creation.Helper.INVAR;
import static jass.modern.core.compile.creation.Helper.SEPARATOR;
import static jass.modern.core.compile.creation.Helper.addMetaInfo;
import static jass.modern.core.compile.creation.Helper.validateCodeVisibility;
import jass.modern.Context;
import jass.modern.Invariant;
import jass.modern.InvariantDefinitions;
import jass.modern.core.compile.creation.IContractCreator.ContractTypes;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.ContractExecutableElement;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.Elements;

import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

@ContractTypes( {Invariant.class, InvariantDefinitions.class })
public class InvariantCreator implements IContractCreator {

	private static final String INVARIANTS_NAME = InvariantDefinitions.class.getName();
	
	@SuppressWarnings("unchecked")
	@Override
	public void create(IAnnotation annotation, IType parent, DiagnosticListener<JavaFileObject> diagnostics) {
		
		int n = 1;
		
		if(annotation.getSimpleName().equals(INVARIANTS_NAME)) {
			
			List<IAnnotation> annotations = Elements.getDefaultValue(annotation, List.class);
			for (IAnnotation invar : annotations) {
				createInvariant(invar, parent, n++, diagnostics);
			}
			
		} else {
			createInvariant(annotation, parent, n, diagnostics);
		}
	}

	private void createInvariant(IAnnotation annotation, IType parent, int n,
			DiagnosticListener<JavaFileObject> diagnostics) {
		
		// (1) discover annotation target...
		IElement target = annotation;
		do{
			target = target.getEnclosingElement();
			
		}while(target instanceof IAnnotation || target instanceof IAnnotationValue || 
				target == null);
		
		// (2) determine visibility values
		IAnnotationValue visibilityValue = annotation.getValue("visibility");
		IAnnotationValue codeValue = annotation.getDefaultValue();
		String code = Elements.getValue(codeValue, String.class);
		
		// (3) create contract element
		String name = target.getSimpleName() + SEPARATOR + INVAR + SEPARATOR + n;
		IExecutable contractElement = new ContractExecutableElement(name, 
				target, codeValue, code);
		contractElement.addModifier(Contracts.getContractVisibiliy(
				visibilityValue, target).toModifier());
		Context context = Elements.getValue(annotation, "context", Context.class, Context.INSTANCE);
		if(context != Context.INSTANCE) 
			contractElement.addModifier(Modifier.STATIC);
		
		// (4) validate contract element
//		validateContractVisibility(visibilityValue, target, diagnostics);
		validateCodeVisibility(visibilityValue, codeValue, target, diagnostics);
		addMetaInfo(contractElement, annotation, "msg", code);

		parent.addEnclosedElement(contractElement);
	}
}

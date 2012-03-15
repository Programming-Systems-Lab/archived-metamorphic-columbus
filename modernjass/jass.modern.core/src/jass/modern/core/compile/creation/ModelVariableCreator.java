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

import static jass.modern.core.compile.creation.Helper.MODEL;
import static jass.modern.core.compile.creation.Helper.SEPARATOR;
import jass.modern.Model;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.creation.IContractCreator.ContractTypes;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.ExecutableElement;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.core.util.JavaUtil;

import java.io.IOException;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

@ContractTypes( Model.class )
public class ModelVariableCreator implements IContractCreator {
	
	private static String SUFFIX = SEPARATOR + MODEL;
	
	private ModelVariableHelper fModelVariableHelper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	
	public void create(IAnnotation annotation, IType parent, DiagnosticListener<JavaFileObject> diagnostics) {
		
		String name = Elements.getValue(annotation, "name", String.class);
		Class<?> type = Elements.getValue(annotation, "type", Class.class);
		
		// (1) check that data is valid
		if(name == null || type == null) {
			diagnostics.report(new ContractDiagnostic(annotation, "Model definition " +
					"is invalid. Type or name missing", Kind.ERROR));
			
			return;
		}
		
		// (2.0) check that name is a valid identifier
		if(! JavaUtil.isJavaIdentifier(name)) {
			diagnostics.report(new ContractDiagnostic(annotation.getValue("name"), 
					name + " is not a valid Java identifier.", Kind.ERROR));
		}
		
		// (2.1) check no such element exists
		IExecutable element = Elements.filterFirst(name + SUFFIX, IExecutable.class, parent.getEnclosedElements());
		if(element != null && Elements.isAbstract(element)) {
				
			diagnostics.report(new ContractDiagnostic(annotation, "An element with the name '" + 
					name + "' does already exist in type " + parent.getQualifiedName(), Kind.ERROR));
			
			return;
		}
		
		// (2.2) check no model variable with name exists in a supertype
		String superTypeName = parent.getSuperclass();
		if(superTypeName != null) {
			try {
				IType superType = new TypeFactory(true).createType(superTypeName, 
						ContractJavaCompiler.getInstance());
				
				if(superType != null &&
						fModelVariableHelper.getAllModelVariables(superType).contains(name)) {
					
					diagnostics.report(new ContractDiagnostic(annotation, "A model variable with name " +
							name + " does already exist in one of the supertypes", Kind.ERROR));
					
					return;
				}
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		// (2.3) check type is abstract or model var is represented
		boolean _abstract = Elements.isAbstract(parent);
		boolean _represented = fModelVariableHelper.getDeclaredRepresentsDefinitions(parent).contains(name);
		if(!_abstract && !_represented) {
			diagnostics.report(new ContractDiagnostic(annotation, "Type " + 
					parent.getQualifiedName() + " must be abstract or " + name + 
					" must be represented.", Kind.ERROR));
			
			return;
		}
		
		// (3) create element
		IExecutable contract = new ExecutableElement(name + SUFFIX, type.getCanonicalName());
		contract.addModifier(Modifier.PUBLIC);
		contract.addModifier(Modifier.ABSTRACT);
		
		parent.addEnclosedElement(contract);
	}
}

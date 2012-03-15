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
import jass.modern.Represents;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.creation.IContractCreator.ContractTypes;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.ContractExecutableElement;
import jass.modern.core.util.Elements;

import java.util.Map;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

@ContractTypes(Represents.class)
public class RepresentsCreator implements IContractCreator {

	private ModelVariableHelper fModelVariableHelper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	
	public void create(IAnnotation annotation, IType parent, DiagnosticListener<JavaFileObject> diagnostics) {
		
		String name = Elements.getValue(annotation, "name", String.class);
		String code = Elements.getValue(annotation, "by", String.class);
		
		if(code == null) {
			IVariable field = Elements.getParent(IVariable.class, annotation);
			code = field.getSimpleName();
		}
		
		if(name == null && code == null) 
			return;
		
		if(code == null) {
			diagnostics.report(new ContractDiagnostic(annotation, 
					"The attribute 'by' must be specified.", Kind.ERROR));
			
			return;
		}
		
		// resolve model variable type
		Map<String, Class<?>> modelVariables = fModelVariableHelper.getAllModelVariables2(parent);
		Class<?> type = modelVariables.get(name);
		
		if(type == null) {
			diagnostics.report(new ContractDiagnostic(annotation, "Could not find the " +
					"definition of model variable " + name, Kind.ERROR));
			
			return;
		}

		String suffix = SEPARATOR + MODEL;
		
		// (1) remove method which was created by the model def
		IExecutable oldContract = Elements.filterFirst(name + suffix, 
				IExecutable.class, parent.getEnclosedElements());
		if(oldContract != null) {
			parent.removeEnclosedElement(oldContract);
		}
		
		// (2) add contract
		IExecutable contract = new ContractExecutableElement(name + suffix, type.getCanonicalName(), code);
		contract.addModifier(Modifier.PUBLIC);
		parent.addEnclosedElement(contract);
	}
}

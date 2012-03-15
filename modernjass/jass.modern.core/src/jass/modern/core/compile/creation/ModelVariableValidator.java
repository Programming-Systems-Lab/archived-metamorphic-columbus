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

import jass.modern.Represents;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IType;
import jass.modern.core.util.ElementScanner;
import jass.modern.core.util.Elements;

import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

/**
 * Validates that subtypes of type defines an un-assigned
 * model variables have either a corresponding
 * {@link Represents}-clause or are abstract.
 * <br /><br />
 * This class depends on the data stored in the
 * {@link ModelVariableHelper}.
 *
 * @author riejo
 */
public class ModelVariableValidator extends ElementScanner<DiagnosticListener<JavaFileObject>>{

	private ModelVariableHelper fModelVariableHelper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	
	@Override
	public void visit(IType element, DiagnosticListener<JavaFileObject> diagnosis) {
		
		Set<String> modelVariables = fModelVariableHelper.getAllModelVariables(element);
		Set<String> representsDefinitions = fModelVariableHelper.getAllRepresentsDefinitions(element);
		
		modelVariables.removeAll(representsDefinitions);
		
		boolean _abstract = Elements.isAbstract(element);
		if(!modelVariables.isEmpty() && !_abstract) {
			
			diagnosis.report(new ContractDiagnostic((IAnnotationValue) null, 
					element.getQualifiedName()  + " must assign model variables " + 
					modelVariables.toString() + " or must be declared as abstract.", Kind.ERROR)); 
		}
	}
}

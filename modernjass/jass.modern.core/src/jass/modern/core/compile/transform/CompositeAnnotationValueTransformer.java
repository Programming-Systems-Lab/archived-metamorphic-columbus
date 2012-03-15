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
import jass.modern.core.model.IAnnotationValue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;


public class CompositeAnnotationValueTransformer implements
		IAnnotationValueTransformer {

	private List<IAnnotationValueTransformer> fTranslators = new LinkedList<IAnnotationValueTransformer>();
	
	public CompositeAnnotationValueTransformer(IAnnotationValueTransformer... translators) {
		this(Arrays.asList(translators));
	}
	
	public CompositeAnnotationValueTransformer(List<IAnnotationValueTransformer> translators) {
		fTranslators.addAll(translators);
	}

	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {
		
		for (IAnnotationValueTransformer translator : fTranslators) {
			try {
				translator.translate(value, diagnostics);
				
			} catch(Exception e) {
				e.printStackTrace();
				diagnostics.report(new ContractDiagnostic(value, "The annotation translator " +
						translator.getClass().getName() + " failed with error " + 
						e.getMessage(), Kind.WARNING));
			}
		}
	}
}

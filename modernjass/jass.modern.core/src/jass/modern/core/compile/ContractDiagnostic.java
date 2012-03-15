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
package jass.modern.core.compile;

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationReference;
import jass.modern.core.model.IAnnotationValue;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ContractDiagnostic implements Diagnostic<JavaFileObject> {

	private IAnnotationReference fAnnotationReference;
	private String fMessage;
	private Kind fKind;
		
	public ContractDiagnostic(IAnnotationValue value, String message, Kind kind) {
		if(value != null && value instanceof IAnnotationReference)
			fAnnotationReference = (IAnnotationReference) value;
		
		fMessage = message;
		fKind = kind;
	}
	
	public ContractDiagnostic(IAnnotation annotation, String message, Kind kind) {
		this(annotation.getDefaultValue(), message, kind);
		
		if(fAnnotationReference == null && 
				annotation instanceof IAnnotationReference) {
		
			fAnnotationReference = (IAnnotationReference) annotation;
		}
	}
	
	public Kind getKind() {
		return fKind;
	}

	public String getMessage(Locale locale) {
		return fMessage;
	}

	public IAnnotationReference getAnnotationReference() {

		return fAnnotationReference;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((fAnnotationReference == null) ? 0 : fAnnotationReference
						.hashCode());
		result = prime * result + ((fKind == null) ? 0 : fKind.hashCode());
		result = prime * result
				+ ((fMessage == null) ? 0 : fMessage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ContractDiagnostic other = (ContractDiagnostic) obj;
		if (fAnnotationReference == null) {
			if (other.fAnnotationReference != null)
				return false;
		} else if (!fAnnotationReference.equals(other.fAnnotationReference))
			return false;
		if (fKind == null) {
			if (other.fKind != null)
				return false;
		} else if (!fKind.equals(other.fKind))
			return false;
		if (fMessage == null) {
			if (other.fMessage != null)
				return false;
		} else if (!fMessage.equals(other.fMessage))
			return false;
		return true;
	}

	public String getCode() {
		return null;
	}

	public long getColumnNumber() {
		return 0;
	}

	public long getEndPosition() {
		return 0;
	}

	public long getLineNumber() {
		return 0;
	}

	public long getPosition() {
		return 0;
	}

	public JavaFileObject getSource() {
		return null;
	}

	public long getStartPosition() {
		return 0;
	}
}

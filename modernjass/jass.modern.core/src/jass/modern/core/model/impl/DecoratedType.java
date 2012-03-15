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
/**
 * 
 */
package jass.modern.core.model.impl;


import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class DecoratedType extends Type {

	private boolean fErrors;
	
	private List<Diagnostic<? extends JavaFileObject>> fDiagnostic;
	
	public DecoratedType(Type other, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		super(other);
		fDiagnostic = diagnostics;
		
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
			if(diagnostic.getKind() == Diagnostic.Kind.ERROR) {
				fErrors = true;
				break;
			}
		}
		
	}
	
	public boolean hasErrors() {
		return fErrors;
	}

	public List<Diagnostic<? extends JavaFileObject>> getDiagnostic() {
		return fDiagnostic;
	}
}

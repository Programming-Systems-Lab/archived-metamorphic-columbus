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
package jass.modern.core.model.impl;

import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;

public class ContractExecutableElement extends ExecutableElement implements IContractExecutable {
	
	public static final String RETURN_STAT = "RETURN_STAT";
	
	private static final String CODE = "CODE";

	private static final String CODE_PATTERN = 
		"try { " + CODE + " } catch(Exception _specerror) { throw new jass.modern.core.SpecificationError(_specerror); }"; 
	
	private IAnnotationValue fContract;
	
	private String fCode;	
	
	public ContractExecutableElement(String name, IElement element, 
			IAnnotationValue contract, String code) {
		
		super(name, "boolean");
		fConstructor = false;
		initDefaults(name, element, contract, code);
	}
	
	public ContractExecutableElement(String name, IExecutable element, 
			IAnnotationValue contract, String code) {
		
		super(name, "boolean");
		fExceptions = new LinkedList<String>(element.getExceptions());
		for (IVariable parameter : element.getParameters()) {
			addEnclosedElement(parameter);
		}
		initDefaults(name, element, contract, code);
	}

	public ContractExecutableElement(String name, String returnType, String code) {
		super(name, returnType);
		wrapAndSetCode(code);
	}
	
	private void initDefaults(String name, IElement original, 
			IAnnotationValue contract, String code) {
		
		// (1) copy modifiers
		fEnclosingElement = original.getEnclosingElement();
		fModifier = new HashSet<Modifier>(original.getModifiers());
		fModifier.remove(Modifier.ABSTRACT);
		fModifier.remove(Modifier.TRANSIENT);
		
		// (2) set contract
		fContract = contract;
		
		// (3) set code
		wrapAndSetCode(code);
	}

	private void wrapAndSetCode(String code) {
		code = code + ";";
		if(code.contains(RETURN_STAT)) {
			code = code.replaceAll(RETURN_STAT, Matcher.quoteReplacement("return"));
			
		} else {
			code = "return " + code;
		}
		setCode(code);
	}

	public void setCode(String code) {
		fCode = CODE_PATTERN.replaceAll(CODE, Matcher.quoteReplacement(code));
	}
	
	public String getCode() {
		return fCode;
	}

	public IAnnotationValue getContract() {
		return fContract;
	}
}

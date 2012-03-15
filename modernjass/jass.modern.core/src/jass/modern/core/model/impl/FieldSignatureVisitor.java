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

import jass.modern.core.util.EmptySignatureVisitor;

import org.objectweb.asm.signature.SignatureVisitor;

public class FieldSignatureVisitor extends EmptySignatureVisitor {
	
	private StringBuffer fBuffer = new StringBuffer();
	private int hasSeenTypeArgument = 0;

	/**
	 * The type of the field
	 */
	@Override
	public void visitClassType(String name) {
		if(hasSeenTypeArgument == 0)
			return;
			
		fBuffer.append(name);
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		
		hasSeenTypeArgument += 1;
		return this;
	}

	public String getGenericSignature() {
		return "<" + fBuffer + ">";
	}
}

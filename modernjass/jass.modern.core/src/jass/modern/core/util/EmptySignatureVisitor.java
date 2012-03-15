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
package jass.modern.core.util;

import org.objectweb.asm.signature.SignatureVisitor;

public class EmptySignatureVisitor implements SignatureVisitor {

	public SignatureVisitor visitArrayType() {
		return null;
	}

	public void visitBaseType(char descriptor) {
	}

	public SignatureVisitor visitClassBound() {
		return null;
	}

	public void visitClassType(String name) {
	}

	public void visitEnd() {
	}

	public SignatureVisitor visitExceptionType() {
		return null;
	}

	public void visitFormalTypeParameter(String name) {
	}

	public void visitInnerClassType(String name) {
	}

	public SignatureVisitor visitInterface() {
		return null;
	}

	public SignatureVisitor visitInterfaceBound() {
		return null;
	}

	public SignatureVisitor visitParameterType() {
		return null;
	}

	public SignatureVisitor visitReturnType() {
		return null;
	}

	public SignatureVisitor visitSuperclass() {
		return null;
	}

	public void visitTypeArgument() {
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		return null;
	}

	public void visitTypeVariable(String name) {
	}


}

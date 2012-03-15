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
package jass.modern.core.bytecode.contracts.handle;

import jass.modern.core.bytecode.contracts.ContractAnalyzer;

import org.objectweb.asm.tree.MethodNode;

public class OldValueContractHandle extends AbstractContractHandle {

	private String fKey;
	private String fMethodName;
	private String fMethodDesc;
	
	public OldValueContractHandle(String className, MethodNode contractMethod) {
		super(className, contractMethod);
		fKey = ContractAnalyzer.getContractIdentifierAsString(contractMethod);
		fMethodName = ContractAnalyzer.getTargetName(contractMethod);
		fMethodDesc = contractMethod.desc;
	}

	public String getKey() {
		return fKey;
	}

	public String getMethodName() {
		return fMethodName;
	}

	public String getMethodDesc() {
		return fMethodDesc;
	}
}

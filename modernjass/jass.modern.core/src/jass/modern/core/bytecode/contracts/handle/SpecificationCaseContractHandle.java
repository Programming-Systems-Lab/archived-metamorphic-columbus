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

public class SpecificationCaseContractHandle extends AbstractContractHandle {

	protected int fIdentifier;
	protected String fMethodName;
	protected String fMethodDesc;
	protected ContractTarget fTarget;
	
	public SpecificationCaseContractHandle(String className,
			MethodNode contractMethod) {
		
		super(className, contractMethod);
		fIdentifier = ContractAnalyzer.getContratIdentifier(contractMethod);
		fMethodName = ContractAnalyzer.getTargetName(contractMethod);
		fTarget = ContractAnalyzer.getContractTarget(contractMethod);
		
		fMethodDesc = contractMethod.desc;
	}

	protected SpecificationCaseContractHandle(SpecificationCaseContractHandle handle) {
		super(handle.getClassName(), handle.getContractMethod());
		
		fIdentifier = handle.getIdentifier();
		fMethodName = handle.getMethodName();
		fMethodDesc = handle.getMethodDesc();
		fTarget = handle.getTarget();
	}
	
	public int getIdentifier() {
		return fIdentifier;
	}

	public String getMethodName() {
		return fMethodName;
	}

	public String getMethodDesc() {
		return fMethodDesc;
	}

	public ContractTarget getTarget() {
		return fTarget;
	}
	
}

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
package jass.modern.core.bytecode.contracts;

import jass.modern.core.bytecode.contracts.handle.RepresentsContractHandle;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class SpecificationClassAdapter extends ClassAdapter {

	private boolean fAbstract;
	private String fClassName;
	private LinkedList<String> fTypeHierarchy;
	private ContractHandleAccessor fContractHandleAccessor;
	
	private ContractCodePool instance = ContractCodePool.getInstance();
	
	public SpecificationClassAdapter(ClassVisitor cv, LinkedList<String> typeHierarchy) {
		super(cv);
		fTypeHierarchy = typeHierarchy;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		
		super.visit(version, access, name, signature, superName, interfaces);
		fClassName = name;
		fAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
				
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if((access & Opcodes.ACC_ABSTRACT) != 0)
			return mv;
		
		if( (access & Opcodes.ACC_NATIVE) != 0)
			return mv;
		
		boolean _static = (access & Opcodes.ACC_STATIC) != 0;
		fContractHandleAccessor = new ContractHandleAccessor(fTypeHierarchy, 
				_static, name, desc);
		
		return new SpecificationMethodAdapter(this, mv, fContractHandleAccessor, 
				access, name, desc);
	}
	
	
	@Override
	public void visitEnd() {
		String contractClassName = getClassName().replace('/', '.');
		
		List<RepresentsContractHandle> represents = instance.get(contractClassName);
		for (RepresentsContractHandle handle : represents) {
			
			MethodNode contractMethod = handle.getContractMethod();
			contractMethod.accept(new ContractCodeCleaner(getParent(), 
					getClassName(), getTypeHierarchy()));
		}
		
		super.visitEnd();
	}

	public String getClassName() {
		return fClassName;
	}
	
	/**
	 * Returns the parent class visitor to bypass this vistor.
	 * @return
	 */
	public ClassVisitor getParent() {
		return cv;
	}
	
	public LinkedList<String> getTypeHierarchy(){
		return fTypeHierarchy;
	}

	public boolean isAbstract() {
		return fAbstract;
	}
}

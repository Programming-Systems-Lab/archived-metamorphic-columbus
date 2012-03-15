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

import static jass.modern.core.util.TypeDescriptors.methodOld;
import static jass.modern.core.util.TypeDescriptors.methodPushExceptionalPostCondition;
import static jass.modern.core.util.TypeDescriptors.methodPushInvariant;
import static jass.modern.core.util.TypeDescriptors.methodPushPostCondition;
import static jass.modern.core.util.TypeDescriptors.methodPushPreCondition;
import static jass.modern.core.util.TypeDescriptors.typeContractContext;
import jass.modern.core.bytecode.contracts.handle.IContractHandle;
import jass.modern.core.bytecode.contracts.handle.InvariantContractHandle;
import jass.modern.core.bytecode.contracts.handle.OldValueContractHandle;
import jass.modern.core.bytecode.contracts.handle.SpecificationCaseContractHandle;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public class SpecificationMethodAdapter extends AbstractSpecificationMethodAdapter {

	public static class ContractOwner {
		
		private Type fType;
		private Method fMethod;
		private IContractHandle fHandle;
		
		public ContractOwner(Type type, Method method, IContractHandle handle) {
			fType = type;
			fMethod = method;
			fHandle = handle;
		}
		
		public Type getType() {
			return fType;
		}
		
		public Method getMethod() {
			return fMethod;
		}
	
		public IContractHandle getHandle() {
			return fHandle;
		}
		
		public boolean isStaticContract() {
			return (fHandle.getContractMethod().access & Opcodes.ACC_STATIC) != 0;
		}
	}
	
	private ContractHandleAccessor fContractAccessor;
	
	public SpecificationMethodAdapter(SpecificationClassAdapter adapter, MethodVisitor mv,
			ContractHandleAccessor contractAccessor, int access, String name, String desc) {
		
		super(adapter, mv, access, name, desc);
		fContractAccessor = contractAccessor;
	}

	@Override
	public boolean old(int indexContractContext) {
		
		List<OldValueContractHandle> oldValues = fContractAccessor.getOldHandles();
		
		if(oldValues.isEmpty())
			return false;
		
		for (OldValueContractHandle handle : oldValues) {
			SpecificationMethodAdapter.ContractOwner owner = insertContractMethod(handle);
			
			loadLocal(indexContractContext);
			push(handle.getKey());
			invokeContractMethod(owner);
			invokeVirtual(typeContractContext, methodOld);
		}
		
		return true;
	}
	
	@Override
	public boolean invariants() {
		if(isHelper())
			return false;
				
		List<InvariantContractHandle> invariants = fContractAccessor.getInvariantHandles();
		
		if(invariants.isEmpty())
			return false;
		
		for (InvariantContractHandle handle : invariants) {
			SpecificationMethodAdapter.ContractOwner owner = insertContractMethod(handle);
			
			loadLocal(indexContractContext);
			invokeContractMethod(owner);
			push(handle.getClassName() + ":" + handle.getMessage());
			invokeVirtual(typeContractContext, methodPushInvariant);
		}
		
		return true;
	}

	@Override
	public boolean targetBefore() {

		return targetSpecificationCase(fContractAccessor.getBeforeHandles(), 
				methodPushPreCondition, -1);
	}

	@Override
	public boolean targetAfter(int returnIndex) {

		return targetSpecificationCase(fContractAccessor.getAfterHandles(), 
				methodPushPostCondition, returnIndex);
	}

	@Override
	public boolean targetFinally(int throwIndex) {
		
		return targetSpecificationCase(fContractAccessor.getFinallyHandles(), 
				methodPushExceptionalPostCondition, throwIndex);
	}
	
	private boolean targetSpecificationCase(List<SpecificationCaseContractHandle> handles, 
			Method evalulationMethod, int index) {
		
		if(handles.isEmpty()) 
			return false;
		
		for(SpecificationCaseContractHandle handle : handles) {
			SpecificationMethodAdapter.ContractOwner owner = insertContractMethod(handle);
			
			loadLocal(indexContractContext);
			push(handle.getIdentifier());
			invokeContractMethod(owner, index);
			push(handle.getMessage());
			invokeVirtual(typeContractContext, evalulationMethod);
		}
		
		return true;
	}

	private SpecificationMethodAdapter.ContractOwner insertContractMethod(IContractHandle handle) {
		String ownerName;
		if(handle.getOwners().contains(fClassName)) {
			ownerName = fClassName;
			
//		}else if(!handle.getOwners().isEmpty() && handle.getVisibility() == Visibility.PUBLIC) {
//			ownerName = handle.getClassName();
//			
		} else {
			handle.getContractMethod().accept(new ContractCodeCleaner(
					fSpecificationClassAdapter.getParent(), 
					fSpecificationClassAdapter.getClassName(), 
					fSpecificationClassAdapter.getTypeHierarchy()));
			
			handle.registerOwner(fClassName);
			ownerName = fClassName;
		}
		
		Type type =  Type.getType("L" + ownerName.replace('.', '/') + ";");
		Method method = new Method(
				handle.getContractMethod().name, 
				handle.getContractMethod().desc);
		
		return new ContractOwner(type, method, handle);
	}
	
	/**
	 * Calls a contract method so that its result (a boolean) is 
	 * on top of the stack:
	 * <ul>
	 * <li><code>stack before:</code>
	 * 	<pre> ? </pre>
	 * <li><code>stack after:</code>
	 * 	<pre> ? | boolean </pre>
	 * </ul>
	 * @param owner 
	 * @param firstParam
	 */
	private void invokeContractMethod(ContractOwner owner, int firstParam) {
		boolean _static = isStatic() || owner.isStaticContract();
		if(!_static) 
			loadThis();
		
		if(firstParam != -1) {
			loadLocal(indexContractContext);
			loadLocal(firstParam);
		}
		loadArgs();

		if(!_static) {
			invokeVirtual(owner.getType(), owner.getMethod());
			
		} else {
			invokeStatic(owner.getType(), owner.getMethod());
		}
	}
	
	/**
	 * Calls a zero-args contract method.
	 *  <ul>
	 * <li><code>stack before:</code>
	 * 	<pre> ? </pre>
	 * <li><code>stack after:</code>
	 * 	<pre> ? | return-type of {@link ContractOwner#getMethod() owner} </pre>
	 * </ul> 
	 * @param owner
	 */
	private void invokeContractMethod(ContractOwner owner) {
		boolean _static = isStatic() || owner.isStaticContract();
		if(!_static) 
			loadThis();
		
		if(!_static) {
			invokeVirtual(owner.getType(), owner.getMethod());

		} else {
			invokeStatic(owner.getType(), owner.getMethod());
		}
	}
}

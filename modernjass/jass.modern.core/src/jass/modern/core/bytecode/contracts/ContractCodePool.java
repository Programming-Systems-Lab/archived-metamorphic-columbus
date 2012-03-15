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


import jass.modern.Visibility;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.bytecode.contracts.handle.IContractHandle;
import jass.modern.core.bytecode.contracts.handle.InvariantContractHandle;
import jass.modern.core.bytecode.contracts.handle.OldValueContractHandle;
import jass.modern.core.bytecode.contracts.handle.RepresentsContractHandle;
import jass.modern.core.bytecode.contracts.handle.SpecificationCaseContractHandle;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * The contract container is the central repository for
 * all the codes of all contracts. Contracts are managed
 * using {@linkplain IContractHandle}s. <br />
 * This contract container is meant to be filled by the
 * {@linkplain ContractAnalyzer} which takes a contract
 * class and extracts the bits which represent a contract.
 * <br />
 * To get an instance of the ContractContainer call the
 * {@linkplain #getInstance()} method which retruns
 * a singleton instance.
 * 
 * @author riejo
 */
public class ContractCodePool {
	
	private static ContractCodePool instance;
		
	public static ContractCodePool getInstance() {
		if(instance == null)
			instance = new ContractCodePool();
		
		return instance;
	}
	
	private Map<String, List<SpecificationCaseContractHandle>> fSpecCases = 
		new HashMap<String, List<SpecificationCaseContractHandle>>();
	
	private Map<String, List<InvariantContractHandle>> fInvariants = 
		new HashMap<String, List<InvariantContractHandle>>();
	
	private Map<String, List<OldValueContractHandle>> fOldValues = 
		new HashMap<String, List<OldValueContractHandle>>();

	private Map<String, List<RepresentsContractHandle>> fRepresents =
		new HashMap<String, List<RepresentsContractHandle>>();
	
	/*
	 * protected due to singleton pattern.
	 */
	protected ContractCodePool() {	}
	
	List<SpecificationCaseContractHandle> get(EnumSet<Visibility> visibility, 
			ContractTarget target, String className, boolean _static, 
			String methodName, String methodDesc) {
		
		List<SpecificationCaseContractHandle> list = fSpecCases.get(className);
		if(list == null) 
			return Collections.emptyList();
		
		LinkedList<SpecificationCaseContractHandle> tmp = new LinkedList<SpecificationCaseContractHandle>();
		for (SpecificationCaseContractHandle handle : list) {
			
			if(staticEqual(_static, handle) 
				&& target == handle.getTarget() 
				&& visibility.contains(handle.getVisibility())
				&& handle.getMethodName().equals(methodName)
				&& paramsEqual(
						Type.getArgumentTypes(methodDesc), 
						Type.getArgumentTypes(handle.getMethodDesc()))) {
			
				tmp.add(handle);
			}
		}
		return tmp;	
	}
	
	/**
	 * 
	 * @param visibility
	 * @param className
	 * @return
	 */
	List<InvariantContractHandle> get(EnumSet<Visibility> visibility, 
			String className, boolean _static) {
		
		List<InvariantContractHandle> invariants = fInvariants.get(className);
		if(invariants == null)
			return Collections.emptyList();
		
		List<InvariantContractHandle> tmp = new LinkedList<InvariantContractHandle>();
		
		for (InvariantContractHandle handle : invariants) {
			
			if(staticEqual(_static, handle) && visibility.contains(handle.getVisibility())) {
				tmp.add(handle);
			}
		}
		
		return  tmp;
	}

	List<OldValueContractHandle> get(String className, String methodName, boolean _static) {
		
		List<OldValueContractHandle> oldValues = fOldValues.get(className);
		if(oldValues == null)
			return Collections.emptyList();
		
		List<OldValueContractHandle> tmp = new LinkedList<OldValueContractHandle>();
		
		for (OldValueContractHandle handle : oldValues) {
			
			if(staticEqual(_static, handle) && 
					handle.getMethodName().equals(methodName)) {
				
				tmp.add(handle);
			}
		}
		
		return tmp;
	}
	
	public List<RepresentsContractHandle> get(String className) {
		
		List<RepresentsContractHandle> represents = fRepresents.get(className);
		if(represents == null) 
			return Collections.emptyList();
		
		return represents;
	}
	
	/**
	 * Determines if a contract handle can be used in a static context
	 * or not. This is:
	 * <li>
	 * <ol>For static methods, only static contracts can be used
	 * <ol>For instance methods, static and instance methods can be used
	 * </li>
	 * 
	 * @param _static
	 * @param handle
	 * @return
	 */
	private boolean staticEqual(boolean _static, IContractHandle handle) {
		
		boolean contractStatic = (handle.getContractMethod().access & Opcodes.ACC_STATIC) != 0;
		
		return !_static || contractStatic;
//		return ! (contractStatic ^ _static);
	}
	
	private boolean paramsEqual(Type[] type1, Type[] type2) {
		int len1 = type1.length;
		int len2 = type2.length;
		
		if(len1 == len2)
			return Arrays.equals(type1, type2);
		
		if(len2 -  len1 != 2)
			return false;
		
		boolean b = true;
		for(int i=0; i<len1 && b; i++)
			b &= type1[i].equals(type2[i+2]);
			
		return b;
	}

	public void put(IContractHandle handle) {
		String key = handle.getClassName();
		
		if(handle instanceof SpecificationCaseContractHandle) {
			ensureKey(key, fSpecCases).add( (SpecificationCaseContractHandle) handle);
		
		} else if(handle instanceof InvariantContractHandle) {
			ensureKey(key, fInvariants).add( (InvariantContractHandle) handle);
		
		} else if(handle instanceof OldValueContractHandle) {
			ensureKey(key, fOldValues).add( (OldValueContractHandle) handle);
		
		} else if(handle instanceof RepresentsContractHandle) {
			ensureKey(key, fRepresents).add( (RepresentsContractHandle) handle);
		}
	}


	/**
	 * Ensures that the given key references a List of
	 * IContractHandles.
	 */
	private <T> List<T> ensureKey(String key, Map<String, List<T>> map) {
		
		List<T> tmp = map.get(key);
		if(tmp == null) {
			tmp = new LinkedList<T>();
			map.put(key, tmp);
		}
		
		return tmp;
	}

}

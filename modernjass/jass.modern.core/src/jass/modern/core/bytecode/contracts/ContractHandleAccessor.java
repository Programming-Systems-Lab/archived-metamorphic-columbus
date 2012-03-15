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
package jass.modern.core.bytecode.contracts;

import static jass.modern.Visibility.PACKAGE_PRIVATE;
import static jass.modern.Visibility.PRIVATE;
import static jass.modern.Visibility.PROTECTED;
import static jass.modern.Visibility.PUBLIC;
import jass.modern.Visibility;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.bytecode.contracts.handle.InvariantContractHandle;
import jass.modern.core.bytecode.contracts.handle.OldValueContractHandle;
import jass.modern.core.bytecode.contracts.handle.SpecificationCaseContractHandle;
import jass.modern.core.compile.creation.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link ContractHandleAccessor} is used to get contract
 * handles for a certain method. The handles returned by this 
 * accessor are taken from the {@link ContractCodePool}.
 *
 * @author riejo
 */
public class ContractHandleAccessor {
		
		/**
		 * An inherited specification case is declared by an super-type
		 * and inherited to the current type. During inheritance, the
		 * contract identifier changes, + 256.
		 *
		 * @author riejo
		 */
		public static class InheritedSpecificationCase extends SpecificationCaseContractHandle {
			
			public InheritedSpecificationCase(SpecificationCaseContractHandle handle, int id) {
				super(handle);
				setIdentifier(id);
			}
			
			public void setIdentifier(int n) {
				fIdentifier = n;
				fContractMethod.name = fMethodName + Helper.SEPARATOR + 
					fTarget + Helper.SEPARATOR + n;
			}
		}
		
		private ContractCodePool fContracts = ContractCodePool.getInstance();
		
		private boolean fStatic;
		private String fMethodName;
		private String fMethodDesc;
		private List<String> fHierarchy;

		private List<SpecificationCaseContractHandle> fBeforeHandles = new LinkedList<SpecificationCaseContractHandle>();
		private List<InvariantContractHandle> fInvariantHandles = new LinkedList<InvariantContractHandle>();
		private List<OldValueContractHandle> fOldHandles = new LinkedList<OldValueContractHandle>();
		private List<SpecificationCaseContractHandle> fAfterHandles = new LinkedList<SpecificationCaseContractHandle>();
		private List<SpecificationCaseContractHandle> fFinallyHandles = new LinkedList<SpecificationCaseContractHandle>();
		
		public ContractHandleAccessor(List<String> hierarchy, boolean _static, String methodName, 
				String methodDesc) {
			
			fStatic = _static;
			fMethodName = methodName;
			fMethodDesc = methodDesc;
			fHierarchy = hierarchy;
			
			initHierarchy();
		}

		private void initHierarchy() {
			
			int offset = 0;
			Iterator<String> iter = fHierarchy.iterator();
			String thisClassName = iter.next();
			initType(offset, EnumSet.of(PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE), thisClassName);
			
			while(iter.hasNext()) {
				offset += 1 << 7;
				initType(offset, EnumSet.of(PUBLIC, PROTECTED, PACKAGE_PRIVATE), iter.next());
			}
		}
		
		private void initType(int offset, EnumSet<Visibility> visibility, String className) {
			
			/*
			 * rename the current method in case its a constructor
			 */
			if(fMethodName.equals("<init>")) {
				int index = className.lastIndexOf('.');
				fMethodName = className.substring(index + 1, className.length());
			}
				
			fInvariantHandles.addAll(fContracts.get(visibility, className, fStatic));
			
			fBeforeHandles.addAll(updateIdentifier(offset, fContracts.get(
					visibility, ContractTarget.PRE, 
					className, fStatic, fMethodName, fMethodDesc)));
			
			fAfterHandles.addAll(updateIdentifier(offset, fContracts.get(
					visibility, ContractTarget.POST, 
					className, fStatic, fMethodName, fMethodDesc)));
			
			fFinallyHandles.addAll(updateIdentifier(offset, fContracts.get(
					visibility, ContractTarget.SIGNALS_POST, 
					className, fStatic, fMethodName, fMethodDesc)));
			
			fOldHandles.addAll(fContracts.get(className, fMethodName, fStatic));
		}

		private Collection<SpecificationCaseContractHandle> updateIdentifier(
				int offset, Collection<SpecificationCaseContractHandle> handles) {
			
			List<SpecificationCaseContractHandle> newHandles = 
				new ArrayList<SpecificationCaseContractHandle>(handles.size());
			
			for (SpecificationCaseContractHandle contractHandle : handles) {
				newHandles.add(new InheritedSpecificationCase(contractHandle, 
						contractHandle.getIdentifier() + offset));
			}
			
			return newHandles;
		}
		
		public List<InvariantContractHandle> getInvariantHandles() {
			return fInvariantHandles;
		}

		public List<SpecificationCaseContractHandle> getBeforeHandles() {
			return fBeforeHandles;
		}

		public List<SpecificationCaseContractHandle> getAfterHandles() {
			return fAfterHandles;
		}
		
		public List<SpecificationCaseContractHandle> getFinallyHandles() {
			return fFinallyHandles;
		}

		public List<OldValueContractHandle> getOldHandles() {
			return fOldHandles;
		}
	}

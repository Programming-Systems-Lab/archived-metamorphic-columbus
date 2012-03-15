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

import jass.modern.Visibility;
import jass.modern.meta.ContractInfo;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

public class AbstractContractHandle implements IContractHandle {

	protected List<String> fOwners = new LinkedList<String>();
	protected String fClassName;
	protected MethodNode fContractMethod;
	protected String fMessage;
	protected Visibility fVisibility;
	
	public AbstractContractHandle(String className, MethodNode contractMethod) {
		fClassName = className;
		fContractMethod = contractMethod;
		fMessage = initMessage(contractMethod);
		fVisibility = Visibility.parseVisibility(contractMethod.access);
	}
	
	public String getClassName() {
		return fClassName;
	}

	public MethodNode getContractMethod() {
		return fContractMethod;
	}

	public String getMessage() {
		return fMessage;
	}

	public List<String> getOwners() {
		return fOwners;
	}

	public Visibility getVisibility() {
		return fVisibility;
	}

	public void registerOwner(String className) {
		fOwners.add(className);
	}

	@SuppressWarnings("unchecked")
	private String initMessage(MethodNode contractMethod) {
		String defaultMsg = "contract violation";
		
		List<AnnotationNode> annotations = contractMethod.visibleAnnotations;
		if(annotations == null || annotations.isEmpty())
			return defaultMsg;
	

		for (AnnotationNode annotation : annotations) {
			if(annotation.desc.equals(
					Type.getType(ContractInfo.class).getDescriptor())) {
				
				List<?> values = annotation.values;
				if(values == null)
					return defaultMsg;
				
				
				String msg = null;
				for(int i=0; i< values.size(); i += 2) {
					String name = (String) values.get(i);
					if(name.equals("message")) {
						msg = "" + values.get(i + 1);
					}
					if(name.equals("code")) {
						msg = msg == null || msg.length() == 0 ? 
								"" + values.get( i + 1) : msg;
					}
				}
				
				if(msg != null)
					return msg;
			}
		}
		
		return defaultMsg;
	}
}

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

import java.util.List;

import org.objectweb.asm.tree.MethodNode;

/**
 * A contract handle is a handle to a piece of bytecode that
 * is the result of a specification, e.g. an invariant or
 * spec case. <br />
 * <br />
 *
 * @author riejo
 */
public interface IContractHandle {
	
	Visibility getVisibility();
	
	String getClassName();
	
	/**
	 * Return the contract method, e.g. the method
	 * <pre>
	 * public boolean name$invar$0(){
	 * 	return name != null;
	 * }
	 * </pre>
	 * which is created from the invariant
	 * <pre>
	 * &#64;Invariant("name != null")
	 * String name;
	 * </pre>
	 * 
	 * @return Returns the actual contract method.
	 */
	MethodNode getContractMethod();
	
	String getMessage();
			
	void registerOwner(String className);
	
	List<String> getOwners();
}

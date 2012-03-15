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


import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.bytecode.contracts.handle.IContractHandle;
import jass.modern.core.bytecode.contracts.handle.InvariantContractHandle;
import jass.modern.core.bytecode.contracts.handle.OldValueContractHandle;
import jass.modern.core.bytecode.contracts.handle.RepresentsContractHandle;
import jass.modern.core.bytecode.contracts.handle.SpecificationCaseContractHandle;
import jass.modern.core.compile.creation.Helper;
import jass.modern.core.compile.transform.OldTransformer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;

public class ContractAnalyzer extends EmptyVisitor {

	/**
	 * A rather complicated pattern matching all kinds
	 * of names that contract methods can have.
	 * <br /><br />
	 * Example are:
	 * <table border="1">
	 * <tr>
	 * <td><code>Foo$Contract$invar$12</code></td><td>The 12th invariant which was defined at the type Foo</td>
	 * </tr>
	 * <tr>
	 * <td><code>add$pre$1</code></td><td>Pre-condition of method <code>add</code></td>
	 * </tr>
	 * </table>
	 */
	private static final Pattern PATTERN = Pattern.compile(
		"(\\w+(\\$Contract)?)\\$(pre|post|signals|invar|old|model)(\\$([\\d|\\w|\\$]+)|)");
	
	private static int GRP_TARGET_NAME = 1;
	
	private static int GRP_CONTRACT_NAME = 3;
	
	private static int GRP_CONTRACT_ID = 5;
	
	private String fClassName;
	private MethodNode fLastMethodNode;
	private ContractCodePool fContractContainer = ContractCodePool.getInstance();
	
	private int fContractCount;
	private boolean done = false;
	
	@Override
	public void visit(int version, int access, String name,
			String signature, String superName, String[] interfaces) {
		
		fClassName = name.replace('/', '.');
		fContractCount = 0;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		captureLastMethodNode();
		fLastMethodNode = new MethodNode(access, name, desc, signature, exceptions);
		return fLastMethodNode;
	}
	
	@Override
	public void visitEnd() {
		captureLastMethodNode();
		done = true;
	}

	private void captureLastMethodNode() {
		if(fLastMethodNode == null)
			return;
		
		Matcher matcher = PATTERN.matcher(fLastMethodNode.name);
		if(!matcher.matches())
			return;
		
		String target = matcher.group(GRP_CONTRACT_NAME);
		
		IContractHandle handle;
		if(target.equals(Helper.INVAR)) {
			handle = new InvariantContractHandle(
					fClassName, fLastMethodNode);
			
		} else if(target.equals(OldTransformer.OLD)) {
			handle = new OldValueContractHandle(
					fClassName, fLastMethodNode);
			
		} else if(target.equals(Helper.MODEL)){
			handle = new RepresentsContractHandle(
					fClassName, fLastMethodNode);
			
		} else{
			handle = new SpecificationCaseContractHandle(
					fClassName, fLastMethodNode);
		}

		fContractContainer.put(handle);
		fContractCount += 1;
		fLastMethodNode = null;
	}

	public int getContractCount() {
		if(!done) {
			throw new IllegalStateException("Analysis is not done yet. " +
					"ContractCount might still change");
		}
		
		return fContractCount;
	}
	
	public static String getTargetName(MethodNode contractMethod) {
		Matcher m = PATTERN.matcher(contractMethod.name);
		if(m.matches() && m.groupCount() >= GRP_TARGET_NAME)
			return m.group(GRP_TARGET_NAME);
		
		return null;
	}
	
	public static ContractTarget getContractTarget(MethodNode contractMethod) {
		Matcher m = PATTERN.matcher(contractMethod.name);
		if(m.matches() && m.groupCount() >= GRP_CONTRACT_NAME)
			return ContractTarget.parseTarget(m.group(GRP_CONTRACT_NAME));
		
		return null;
	}
	
	public static int getContratIdentifier(MethodNode contractMethod) {
		String tmp = getContractIdentifierAsString(contractMethod);
		
		return tmp == null ? 
				-1 : Integer.parseInt(tmp);
	}
	
	public static String getContractIdentifierAsString(MethodNode contractMethod) {
		Matcher m = PATTERN.matcher(contractMethod.name);
		if(m.matches() && m.groupCount() >= GRP_CONTRACT_ID)
			return m.group(GRP_CONTRACT_ID);
		
		return null;
	}
}

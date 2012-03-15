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


import jass.modern.Helper;
import jass.modern.SpecCase;
import jass.modern.core.runtime.ContractContext;
import jass.modern.core.runtime.IAssertionStatus;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * {@link Type} and {@link Method} constants for various
 * types and methods.
 * 
 * @author riejo
 */
public class TypeDescriptors {
	
	/**
	 * {@link ContractContext}
	 */
	public static final Type typeContractContext = Type.getType(ContractContext.class);
	
	/**
	 * {@link ContractContext#getContractContext(Thread, String, String, String)}
	 */
	public static final Method methodGetContractContext = Method.getMethod(
	"jass.modern.core.runtime.ContractContext " +
	"getContractContext(Thread,String,String,String)");
	
	/**
	 * {@link ContractContext#dispose()}
	 */
	public static final Method methodDisposeContractContext = Method.getMethod(
	"void dispose()");
	
	/**
	 * {@link ContractContext#isBusy()}
	 */
	public static final Method methodIsBusy = Method.getMethod("boolean isBusy()");
	
	/**
	 * {@link ContractContext#pushInvariant(boolean, String)}
	 */
	public static final Method methodPushInvariant = Method.getMethod(
	"void pushInvariant(boolean,String)");
	
	/**
	 * {@link ContractContext#evaluateInvariants()}
	 */
	public static final Method methodEvalInvariants = Method.getMethod(
	"jass.modern.core.runtime.IAssertionStatus evaluateInvariants()");
	
	/**
	 * {@link ContractContext#pushPreCondition(int, boolean, String)}
	 */
	public static final Method methodPushPreCondition = Method.getMethod(
	"void pushPreCondition(int,boolean,String)");
	
	/**
	 * {@link ContractContext#evaluatePreConditions()}
	 */
	public static final Method methodEvalPreConditions = Method.getMethod(
	"jass.modern.core.runtime.IAssertionStatus evaluatePreConditions()");
	
	/**
	 * {@link ContractContext#pushPostCondition(int, boolean, String)}
	 */
	public static final Method methodPushPostCondition = Method.getMethod(
	"void pushPostCondition(int,boolean,String)");
	
	/**
	 * {@link ContractContext#evaluatePostConditions()}
	 */
	public static final Method methodEvalPostConditions = Method.getMethod(
	"jass.modern.core.runtime.IAssertionStatus evaluatePostConditions()");
	
	/**
	 * {@link ContractContext#pushExceptionalPostCondition(int, boolean, String)}
	 */
	public static final Method methodPushExceptionalPostCondition = Method.getMethod(
	"void pushExceptionalPostCondition(int,boolean,String)");
	
	/**
	 * {@link ContractContext#evaluateExceptionalPostConditions()
	 */
	public static final Method methodEvalExceptionalPostConditions = Method.getMethod(
	"jass.modern.core.runtime.IAssertionStatus evaluateExceptionalPostConditions()");
	
	/**
	 * {@link ContractContext#old(String, Object)}
	 */
	public static final Method methodOld = Method.getMethod("void old(String,Object)");
	
	/**
	 * {@link IAssertionStatus}
	 */
	public static final Type typeAssertionStatus = Type.getType(IAssertionStatus.class);
	
	/**
	 * {@link IAssertionStatus#success()}
	 */
	public static final Method methodSuccess = Method.getMethod("boolean success()");
	
	/**
	 * {@link IAssertionStatus#getMessage()}
	 */
	public static final Method methodGetMessage = Method.getMethod("String getMessage()");

	/**
	 * {@link Thread}
	 */
	public static final Type typeThread = Type.getType(Thread.class);
	
	/**
	 * {@link Thread#currentThread()}
	 */
	public static final Method methodCurrentThread = Method.getMethod("Thread currentThread()");
	
	/**
	 * {@link Exception}
	 */
	public static final Type typeException = Type.getType(Exception.class);
	
	/**
	 * {@link Void}
	 */
	public static final Type typeVoid = Type.getType(Void.class);
	
	/**
	 * {@link Helper}
	 */
	public static final Type annotationHelper = Type.getType(Helper.class);
	
	/**
	 * {@link SpecCase}
	 */
	public static final Type annotationSpecCase = Type.getType(SpecCase.class);
}

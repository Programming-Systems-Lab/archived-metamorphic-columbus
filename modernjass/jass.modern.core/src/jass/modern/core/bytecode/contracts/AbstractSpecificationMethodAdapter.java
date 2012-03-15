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


import static jass.modern.core.util.TypeDescriptors.annotationHelper;
import static jass.modern.core.util.TypeDescriptors.methodCurrentThread;
import static jass.modern.core.util.TypeDescriptors.methodDisposeContractContext;
import static jass.modern.core.util.TypeDescriptors.methodEvalExceptionalPostConditions;
import static jass.modern.core.util.TypeDescriptors.methodEvalInvariants;
import static jass.modern.core.util.TypeDescriptors.methodEvalPostConditions;
import static jass.modern.core.util.TypeDescriptors.methodEvalPreConditions;
import static jass.modern.core.util.TypeDescriptors.methodGetContractContext;
import static jass.modern.core.util.TypeDescriptors.methodGetMessage;
import static jass.modern.core.util.TypeDescriptors.methodIsBusy;
import static jass.modern.core.util.TypeDescriptors.methodSuccess;
import static jass.modern.core.util.TypeDescriptors.typeAssertionStatus;
import static jass.modern.core.util.TypeDescriptors.typeContractContext;
import static jass.modern.core.util.TypeDescriptors.typeThread;
import static jass.modern.core.util.TypeDescriptors.typeVoid;
import jass.modern.Helper;
import jass.modern.core.InvariantError;
import jass.modern.core.PostConditionError;
import jass.modern.core.PreConditionError;
import jass.modern.core.runtime.ContractContext;
import jass.modern.core.runtime.IAssertionStatus;
import jass.modern.core.util.TypeDescriptors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

/**
 * An abstract {@link MethodVisitor} which eases
 * the instrumentation of a method with behavioural
 * specification.
 * 
 * @author riejo
 */
public abstract class AbstractSpecificationMethodAdapter extends AdviceAdapter {

	enum ErrorType {
		
		INVARIANT(InvariantError.class), 
		PRE(PreConditionError.class), 
		POST(PostConditionError.class);
		
		private Class<? extends AssertionError> fType;
		
		private ErrorType(Class<? extends AssertionError> clazz) {
			fType = clazz;
		}
		
		public Class<? extends AssertionError> getType(){
			return fType;
		}
		
		public String getInternalTypeName() {
			return fType.getName().replace('.', '/');
		}
	}
	
	/**
	 * The exception handle begin label
	 */
	private Label fExceptionHandlerBegin = new Label();
	
	/**
	 * The end label of the exception handler.
	 */
	private Label fExceptionHandlerEnd = new Label();
	
	/**
	 * The first line number of this method.
	 */
	private Integer fFirstLineNumber = null;
	
	private boolean fStatic = false;
	private boolean fConstructor = false;
	private boolean fStaticInitializer = false;
	private boolean fHelper = false;
	
	protected SpecificationClassAdapter fSpecificationClassAdapter;
	
	protected String fClassName;
	protected String fMethodName;
	protected String fMethodDesc;
	
	protected int indexContractContext;

	public AbstractSpecificationMethodAdapter(SpecificationClassAdapter adapter, MethodVisitor mv, 
			int access, String name, String desc) {
		
		super(mv, access, name, desc);
		fStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
		fConstructor = name.equals("<init>");
		fStaticInitializer = name.endsWith("<clinit>");
		
		/*
		 * Helper <==> constructor of an abstract class or @Helper annotation
		 * see visitAnnotation-method...
		 */
		fHelper = fConstructor && adapter.isAbstract();
		
		fSpecificationClassAdapter = adapter;
		fClassName = adapter.getClassName();
		fMethodName = name;
		fMethodDesc = desc;
		
		indexContractContext = -1;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		
		if(Type.getType(desc).equals(annotationHelper)) {
			fHelper = true;
		}
		
		return null;
	}

	@Override
	protected void onMethodEnter() {
		indexContractContext = createContractContext();
		visitLabel(fExceptionHandlerBegin);
		aroundTargetBefore();
	}

	@Override
	protected void onMethodExit(int opcode) {
		if(opcode == Opcodes.ATHROW)
			return;
		
		aroundTargetAfter();
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		if(fFirstLineNumber == null) {
			fFirstLineNumber  = line;
			super.visitLineNumber(line, fExceptionHandlerBegin);
			
		} else {
			super.visitLineNumber(line, start);
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,	String desc) {
	
		boolean checkInvariants = !isConstructor() && fSpecificationClassAdapter.getTypeHierarchy().contains(owner.replace('/', '.'));
		if(checkInvariants) {
			aroundInvariants();
		}
		super.visitMethodInsn(opcode, owner, name, desc);
		
		if(checkInvariants) {
			aroundInvariants();
		}
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		aroundTargetFinally();		
		
 		super.visitMaxs(maxStack, maxLocals);
	}

	private void aroundTargetBefore() {
		
		Label skipContracts = loadContractContext();
		
		/*
		 * no invariants in the pre-state of constructors
		 * and static initializers
		 */
		if(!isConstructor() && !isStaticInitializer()) {
			aroundInvariants();
		}
			
		boolean evaluate = targetBefore();
		if(evaluate || true) 
			evaluateContractContext(methodEvalPreConditions, ErrorType.PRE);
		
		visitLabel(skipContracts);
	}

	private void aroundTargetAfter() {
		Label skipContracts = loadContractContext();
		
		Type returnType = Type.getReturnType(fMethodDesc);
		if(returnType.equals(Type.LONG_TYPE) || 
				returnType.equals(Type.DOUBLE_TYPE)) {
			
			dup2();
			
		} else if(returnType.equals(Type.VOID_TYPE)) {
			push( (Type) null);
			returnType = typeVoid;
			
		} else {
			
			dup();
		}
		int returnIndex = newLocal(returnType);
		storeLocal(returnIndex);
		
		boolean evaluate = targetAfter(returnIndex);
		if(evaluate || true) 
			evaluateContractContext(methodEvalPostConditions, ErrorType.POST);
		
		aroundInvariants();
		visitLabel(skipContracts);
		disposeContractContext();
	}

	private void aroundTargetFinally() {
		visitLabel(fExceptionHandlerEnd);
		catchException(fExceptionHandlerBegin, fExceptionHandlerEnd, TypeDescriptors.typeException);
		
		int throwIndex = newLocal(TypeDescriptors.typeException);
		storeLocal(throwIndex);
		
		Label skipContracts = loadContractContext();
		
		boolean evaluate = targetFinally(throwIndex);
		if(evaluate || true)
			evaluateContractContext(methodEvalExceptionalPostConditions, ErrorType.POST);
	
		aroundInvariants();
		visitLabel(skipContracts);
		disposeContractContext();
	
		loadLocal(throwIndex);
		throwException();
	}

	private void aroundInvariants() {
		boolean evaluate = invariants();
		if(evaluate) {
			loadLocal(indexContractContext);
			invokeVirtual(typeContractContext, methodEvalInvariants);
			evaluateAssertionStatus();
			assertTrue(ErrorType.INVARIANT);
		}
	}

	abstract boolean old(int indexContractContexet);
	
	/**
	 * Insert contract code to check invariants
	 * @return
	 */
	abstract boolean invariants();

	/**
	 * Insert contract code to check pre-conditions
	 * @return
	 */
	abstract boolean targetBefore();
	
	/**
	 * Insert contract code to check post-conditions
	 * @param returnIndex Index of a local variable storing
	 * 	the return value.
	 * @return
	 */
	abstract boolean targetAfter(int returnIndex);
	
	/**
	 * Insert contract code to check a exceptional 
	 * post-condition. 
	 * @param throwIndex Index of a local variable storing
	 * 	the exception reference.
	 * @return
	 */
	abstract boolean targetFinally(int throwIndex);
	
	/**
	 * 
	 * @return Returns <code>true</code> if the method working
	 * 	with is static, <code>false</code> otherwise.
	 */
	public boolean isStatic() {
		return fStatic;
	}
	
	/**
	 * 
	 * @return Return <code>true</code> iff the method working
	 * 	on is a constructor.
	 */
	public boolean isConstructor() {
		return fConstructor;
	}
	
	/**
	 * 
	 * @return Returns <code>true</code> iff the current method
	 * 	is the static initializer of a class.
	 */
	public boolean isStaticInitializer() {
		return fStaticInitializer;
	}
	
	/**
	 * Reflects if this method is a helper method and, thus, no
	 * invariant checks are performed when invoked.
	 * <ol>
	 * <li>A method is a helper if the {@link Helper}-annotation
	 * 	is present
	 * <li>The constructor of a abstract class is a helper
	 * </ol>
	 * <br />
	 * <em>Note:</em> The value that is retured by this method is 
	 * may change until {@link #visitCode()} has been called.
	 * 
	 * @return Returns <code>true</code> iff the method
	 * 	is a helper-method.
	 */
	public boolean isHelper() {
		return fHelper;
	}
	
	/**
	 * 
	 * @return Returns a label (not visited) which 
	 * 	is a jump target in case the contract context
	 * 	is busy.
	 */
	private Label loadContractContext() {
		if(indexContractContext < 0) {
			indexContractContext = createContractContext();
		}
		
		Label skipContracts = new Label();
		goToLabelIfContextIsBusy(skipContracts);
		return skipContracts;
	}


	/**
	 * Generate the byteocde instructions which are equivalent to:
	 * <pre>
	 * ContractContext cc = ContractContext.getContractContext(
	 * 		Thread.currentThread(), 
	 * 		String, String, String)
	 * </pre>
	 * where the String parameters denote the current {@linkplain #fClassName},
	 * the {@linkplain #fMethodName}, and the {@linkplain #fMethodDesc}.
	 * @see ContractContext#getContractContext(Thread, String, String, String)
	 * 
	 * @return Return the index of the local variable the 
	 * 	ContractContext is store at.
	 */
	private int createContractContext() {
		int tmp = newLocal(typeContractContext);
		invokeStatic(typeThread, methodCurrentThread);
		visitLdcInsn(fClassName);
		visitLdcInsn(fMethodName);
		visitLdcInsn(fMethodDesc);
		invokeStatic(typeContractContext, methodGetContractContext);
		storeLocal(tmp);
		
		old(tmp);
		
		return tmp;
	}

	private void evaluateContractContext(Method evaluationMethod, ErrorType error) {
		loadLocal(indexContractContext);
		invokeVirtual(typeContractContext, evaluationMethod);
		evaluateAssertionStatus();
		assertTrue(error);
	}

	/**
	 * Generate the byteocde instructions which are equivalent to:
	 * <pre>
	 * contractContext.dispose();
	 * </pre>
	 * <em>Note:</em> contractContext is a local varible is 
	 * {@linkplain #fContractContextIndex}.
	 * @see ContractContext#dispose()
	 */
	private void disposeContractContext() {
		loadLocal(indexContractContext);
		invokeVirtual(typeContractContext, methodDisposeContractContext);
	}
	
	/**
	 * Generate the byteocde instructions which are equivalent to:
	 * <pre>
	 * if(!contractContext.isBusy()){
	 * 
	 * }
	 * </pre>
	 * <em>Note:</em> contractContext is a local varible is 
	 * {@linkplain #fContractContextIndex}.
	 * @see ContractContext#isBusy()
	 * 
	 * @param skipContracts The label which marks the end 
	 * 	of the conditional branch (if-block).
	 */
	private void goToLabelIfContextIsBusy(Label skipContracts) {
		loadLocal(indexContractContext);
		invokeVirtual(typeContractContext, methodIsBusy);
		ifZCmp(NE, skipContracts);
	}
	

	/**
	 * Evaluates the {@linkplain IAssertionStatus}. <br />
	 * <ul>
	 * <li><code>stack before:</code>
	 * 	<pre> ? | IAssertionStatus</pre>
	 * <li><code>stack after:</code>
	 * 	<pre> ? | String | boolean</pre>
	 * 	where the <i>String</i> is {@linkplain IAssertionStatus#getMessage()} and 
	 * 	<i>boolean</i> is {@linkplain IAssertionStatus#success()}.
	 * </ul>
	 * 
	 */
	private void evaluateAssertionStatus() {
		dup();
		invokeInterface(typeAssertionStatus, methodGetMessage);
		swap();
		invokeInterface(typeAssertionStatus, methodSuccess);
	}

	/**
	 * Throws an {@linkplain AssertionError} if false is on top of
	 * the stack. <br />
	 * <ul>
	 * <li><code>stack before:</code>
	 * 	<pre> ? | String | boolean </pre>
	 * <li><code>stack after:</code>
	 * 	<pre> ? </pre>
	 * </ul>
	 * @param error The ErrorType which is to throw in case the
	 * 	boolean condition did not hold.
	 */
	private void assertTrue(ErrorType error) {
		// (1) check boolean on stack
		Label nonContractCodeStart = new Label();
		ifZCmp(NE, nonContractCodeStart);
		
		// (2) dispose context and throw AssertionError
		disposeContractContext();
		visitTypeInsn(NEW, error.getInternalTypeName());		// stack: ? | String | AssertionError
		dupX1();	// stack: ? | AssertionError | String | AssertionError
		swap();		// stack: ? | AssertionError | AssertionError | String
		visitMethodInsn(INVOKESPECIAL, error.getInternalTypeName(), "<init>", "(Ljava/lang/Object;)V");
		throwException();
		
		// (3) preceed here if no exception
		visitLabel(nonContractCodeStart);
		pop();	// remove the String from the stack - it is not needed
	}
}

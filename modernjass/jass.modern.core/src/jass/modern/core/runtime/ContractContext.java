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
package jass.modern.core.runtime;


import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The contract context is a helper to evaluate and enable
 * method specifications. At runtime it is the interface
 * between instrumented bytecode and modern jass. <br />
 * 
 * The {@link ContractContext} is reposible for
 * <ul>
 * <li>Ensure no endless recursive calls of 
 * 	<em>method-contract-method-contract-...</em> occur.
 * 	For instance, consider this spec:
 * 	<pre>
 *  &#064;Pre("b() == 1")
 *  int a(){ ... }
 * 
 *  &#064;Pre("a() == 2")
 *  int b(){ ... }
 * 	</pre>
 *  which will end up in <em>a() -> b() -> a() -> b() -> ...</em> 
 *  if contract checking is not disabled from a certain 
 *  point on. The ContractContext will ensure that above 
 *  will be: 
 *  <pre>
 *   a() 
 *     b() [ pre-condition of a] 
 *       a() [pre-condition of b]
 *       	**!! contract checking is disabled now preventing endless recursion !!**
 *       a()RETURN 
 *     b()RETURN 
 *   a()RETURN
 *  </pre>
 *  
 * <li>Stores and evaluates all {@link #evaluateInvariants()}, {@link #evaluatePreConditions()},
 * 	and {@link #evaluatePostConditions(Map))}.
 * <li>Storage for values related to </code>@Old</code>.
 * </ul>
 * 
 * @author riejo
 */
public class ContractContext {

	private final class InvariantAssertionStatus implements IAssertionStatus {
		
		private boolean success = true;
		private final List<String> message = new LinkedList<String>();
		
		public InvariantAssertionStatus(Deque<IAssertionStatus> invariants) {
			
			for (IAssertionStatus status : invariants) {
				boolean b = status.success();
				success &= b;
				
				if(!b) {
					message.add(status.getMessage() + ", ");
				}
			}
		}

		public String getMessage() {
			return message.toString();
		}

		public boolean success() {
			return success;
		}
	}

	private final class PreConditionAssertionStatus implements IAssertionStatus {
		
		private boolean fSuccess;
		private final List<String> fMessages = new LinkedList<String>();
		
		PreConditionAssertionStatus(Map<Integer, IAssertionStatus> preConditions){
			
			fSuccess = preConditions.isEmpty();
			
			for (IAssertionStatus status : preConditions.values()) {
				fSuccess |= status.success();
				
				if(!status.success()) {
					fMessages.add(status.getMessage());
				}
			}
		}

		public boolean success() {
			return fSuccess;
		}

		public String getMessage() {
			return fMessages.toString();
		}
	}

	private final class PostConditionAssertionStatus implements	IAssertionStatus {
		
		private boolean fSuccess = true;
		private final List<String> fMessages = new LinkedList<String>();
		
		
		PostConditionAssertionStatus(Map<Integer, IAssertionStatus> preConditions,
				Map<Integer, IAssertionStatus> postConditions) {
			
			Set<Integer> keys = new HashSet<Integer>();
			keys.addAll(preConditions.keySet());
			keys.addAll(postConditions.keySet());
			
			for (Integer key : keys) {
				IAssertionStatus preValue = preConditions.get(key);
				IAssertionStatus postValue = postConditions.get(key);
				
				if(preValue == null && postValue == null)
					continue;
				
				boolean pre = preValue == null || preValue.success() ;
				boolean post = postValue == null || postValue.success();
				
				boolean tmp = !pre || post;	// <=> (pre => post)
				if(!tmp && postValue != null) {
					fMessages.add(postValue.getMessage());
				}
				
				fSuccess &= tmp;
			}
		}

		public String getMessage() {
			return fMessages.toString();
		}

		public boolean success() {
			return fSuccess;
		}
	}

	private static Map<Long, Deque<String>> contractStacks = new HashMap<Long, Deque<String>>();
	
	private final Deque<IAssertionStatus> fInvariants = new LinkedList<IAssertionStatus>();
	private final Map<Integer, IAssertionStatus> fPreConditions = new HashMap<Integer, IAssertionStatus>();
	private final Map<Integer, IAssertionStatus> fPostConditions = new HashMap<Integer, IAssertionStatus>();
	private final Map<Integer, IAssertionStatus> fExceptionalPostConditions = new HashMap<Integer, IAssertionStatus>();
	
	private final Map<String, Object> fOldValues = new HashMap<String, Object>();
	
	private final Thread fThread;
	private final boolean fIsBusy;
	private final String fContextId;
	
	/**
	 * Factory method to get a contract context based on a <i>thread</i> 
	 * and a <i>method</i>. The <i>method</i> is described by its defining class
	 * (<em>className</em>), its <em>name</em>, and <em>signature</em>.
	 * <br />
	 * <em>Note:</em> Although
	 * this method  returns a contract context is might not be
	 * useable. This is because the same thread checks the contract
	 * of the described method already. In that case 
	 * {@linkplain #isBusy()} return <code>true</code>.
	 * 
	 * @param thread The thread in which this context resides.
	 * @param className The name of the class defining the method.
	 * @param methodName The name of the method.
	 * @param methodDesc The signature of the method. Must be in JNI
	 * 	format. E.g. <code>int bla(Object o)</code> is 
	 * 	<code>(Ljava/lang/Object;)I</code>.
	 * @return
	 */
	public synchronized static ContractContext getContractContext(Thread thread, String className, 
			String methodName, String methodDesc) {
		
		return new ContractContext(thread, className, methodName, methodDesc);
	}
	
	protected ContractContext(Thread thread, String className, String methodName, String methodDesc) {
		fThread = thread;
		
		fContextId = className + "." + methodName + ":" + methodDesc;

		Deque<String> contractStack = ensureKey(thread);
		fIsBusy = contractStack.contains(fContextId);
		contractStack.push(fContextId);
	}
	
	private Deque<String> ensureKey(Thread thread) {
		Deque<String> tmp = contractStacks.get(thread.getId());
		if(tmp == null) {
			tmp = new LinkedList<String>();
			contractStacks.put(thread.getId(), tmp);
		}
		
		return tmp;
	}

	public Thread getThread() {
		return fThread;
	}

	public boolean isBusy() {
		return fIsBusy;
	}

	public void dispose() {
		Deque<String> contractStack = ensureKey(fThread);
		
		if(!contractStack.contains(fContextId))
			throw new IllegalStateException(fContextId + " is not on contract stack");
		
		if(!contractStack.peek().equals(fContextId)) {
			throw new IllegalStateException(fContextId + " is not on top of contract stack");
		}

		contractStack.pop();
		
		fOldValues.clear();
		fInvariants.clear();
		fPreConditions.clear();
		fPostConditions.clear();
		fExceptionalPostConditions.clear();
	}
	
	public IAssertionStatus evaluateInvariants() {
		if(isBusy())
			throw new IllegalStateException();
		
		return new InvariantAssertionStatus(fInvariants);
	}
	
	public IAssertionStatus evaluatePreConditions() {
		if(isBusy())
			throw new IllegalStateException();
		
		return 	new PreConditionAssertionStatus(fPreConditions);
	}

	public IAssertionStatus evaluatePostConditions() {
		return evaluatePostConditions(fPostConditions);
	}
	
	public IAssertionStatus evaluateExceptionalPostConditions() {
		return evaluatePostConditions(fExceptionalPostConditions);
	}
	
	protected IAssertionStatus evaluatePostConditions(final Map<Integer, IAssertionStatus> postConditions) {
		if(isBusy())
			throw new IllegalStateException();
		
		return  new PostConditionAssertionStatus(fPreConditions, postConditions);
	}
	
	public void pushInvariant(boolean value, String message) {
		if(isBusy())
			throw new IllegalStateException();
		
		fInvariants.push(new AssertionStatus(value, message));
	}
	
	public void pushPreCondition(int n, boolean value, String message) {
		if(isBusy())
			throw new IllegalStateException();
		
		fPreConditions.put(n, new AssertionStatus(value, message));
	}
	
	public void pushPostCondition(int n, boolean value, String message) {
		if(isBusy())
			throw new IllegalStateException();
		
		fPostConditions.put(n, new AssertionStatus(value, message));
	}
	
	public void pushExceptionalPostCondition(int n, boolean value, String message) {
		if(isBusy())
			throw new IllegalStateException();
		
		fExceptionalPostConditions.put(n, new AssertionStatus(value, message));
	}
	
	public void old(String key, Object value) {
		fOldValues.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T old(String key) {
		return (T) fOldValues.get(key);
	}
}

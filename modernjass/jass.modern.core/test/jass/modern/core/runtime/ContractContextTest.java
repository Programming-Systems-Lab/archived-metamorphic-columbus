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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContractContextTest {

	ContractContext fContractContext;
	
	@Before
	public void setUp() throws Exception {
		fContractContext = ContractContext.getContractContext(Thread.currentThread(), "some.Object", "add", "()V");
		assertFalse(fContractContext.isBusy());
	}

	@After
	public void tearDown() {
		fContractContext.dispose();
	}
	
	@Test
	public void testGetContractContext() {
		ContractContext c1 = ContractContext.getContractContext(Thread.currentThread(), "some.Object", "add", "()V"); 
		assertTrue(c1.isBusy());
		c1.dispose();

		c1 = ContractContext.getContractContext(new Thread(), "some.Object", "add", "()V");
		assertFalse(c1.isBusy());
		c1.dispose();
	}

	@Test
	public void testGetThread() {
		assertNotNull(fContractContext.getThread());
		assertTrue(fContractContext.getThread() == Thread.currentThread());
	}

	@Test
	public void testIsBusy() {
		assertFalse(fContractContext.isBusy());
	}

	@Test()
	public void testDispose() {
		Thread t = new Thread();
		ContractContext localCC = ContractContext.getContractContext(t, "cls", "method", "()V");
		localCC.dispose();
		try {
			localCC.dispose();
			fail();
			
		} catch(IllegalStateException e) {	}
	}
	
	@Test
	public void testDisposeMulti() {
		Thread t = new Thread();
		ContractContext cc1 = ContractContext.getContractContext(t, "cls", "m", "()V");
		ContractContext cc2 = ContractContext.getContractContext(t, "cls", "m2", "()V");
		
		cc2.dispose();
		cc1.dispose();
	}
	
	@Test
	public void testEvaluatePreConditions() {
		fContractContext.pushPreCondition(0, true, "foo");
		fContractContext.pushPreCondition(1, false, "bar");
		fContractContext.pushPreCondition(2, true, "foo.bar");
		
		assertTrue(fContractContext.evaluatePreConditions().success());
	}

	@Test
	public void testEvaluatePostConditions() {
		fContractContext.pushPreCondition(10, true, "foo");
		fContractContext.pushPostCondition(10, true, "bar");
		fContractContext.pushPostCondition(7, true, "foo.bar");
		assertTrue(fContractContext.evaluatePostConditions().success());
		
		fContractContext.pushPostCondition(8, false, "foo.bar");
		IAssertionStatus status = fContractContext.evaluatePostConditions();
		assertFalse(status.success());
		assertEquals("[foo.bar]", status.getMessage());
		
		ContractContext cc = ContractContext.getContractContext(Thread.currentThread(), "bla", "bla", "()V");
		assertTrue(cc.evaluatePostConditions().success());
		cc.dispose();
	}

	@Test
	public void testPushPreCondition() {
		fContractContext.pushPreCondition(20, true, "");
	}

	@Test
	public void testPushPostCondition() {
		fContractContext.pushPostCondition(0, true, "");
		fContractContext.pushPostCondition(20, true, "");
	}

	@Test
	public void testPreconditions() {
		fContractContext.pushPreCondition(1, true, "");
		fContractContext.pushPreCondition(101, false, "");
		boolean actual = fContractContext.evaluatePostConditions().success();
		assertEquals(true, actual);
	}
	
	/**
	 * Test something like:
	 * Also({
	 * SpecCase( pre = "false", post = "true", signals = Exception.class, signalsPost = "false"), 
	 * SpecCase( pre = "true", signals = NPE.class) }) 
	 */
	@Test
	public void testExceptionalPostConditions() {
		fContractContext.pushPreCondition(1, false, "o != null");
		fContractContext.pushExceptionalPostCondition(1, false, "no exceptions");
		fContractContext.pushPreCondition(2, true, "o == null");
		fContractContext.pushExceptionalPostCondition(2, true, "true");
		
		assertTrue(fContractContext.evaluatePreConditions().success());
		assertTrue(fContractContext.evaluateExceptionalPostConditions().success());
	}
}

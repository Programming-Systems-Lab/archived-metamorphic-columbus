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
package jass.modern.core.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jass.modern.core.apt.IRevealableAnnotationValue;
import jass.modern.core.compile.creation.ContractCreationController;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.AnnotationProcessingTestUtil;
import jass.modern.core.util.Elements;
import jass.modern.meta.ContractInfo;
import jass.modern.tests.Helper;
import jass.modern.tests.RuntimeDummyBuffer;

import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;


public class ContractCreationControllerTest {

	ContractCreationController fContractCreation;

	protected IType fType;
	protected IExecutable fMethodAdd;

	@AfterClass
	public static void tearDown() {
	}
	
	@Before
	public void setUp() throws Exception {
		fContractCreation = new ContractCreationController();
		
		fType = AnnotationProcessingTestUtil.runDefaultProcessor(
				Helper.getAbsoluteFilePath(RuntimeDummyBuffer.class)[0]);

		fType.accept(fContractCreation, new DiagnosticCollector<JavaFileObject>());
		fMethodAdd = Elements.filter("add", IExecutable.class, fType.getEnclosedElements()).get(0);
	}
	
	@Test
	public void testModel() {
		IExecutable modelExec = Elements.filterFirst("bla$model", IExecutable.class, fType.getEnclosedElements());
		assertNotNull(modelExec);
		
		assertTrue(modelExec.getModifiers().contains(Modifier.PUBLIC));
	}
	
	@Test
	public void testRepresents() {
		IContractExecutable exec = Elements.filterFirst("foo$model", IContractExecutable.class, fType.getEnclosedElements());
		assertNotNull(exec);
		
		assertEquals("java.lang.Integer", exec.getReturnType());
	}
	
	@Test
	public void testInvariant() {
		List<? extends IElement> fIntInvariant = Elements.filter("fInt*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(1, fIntInvariant.size());
		
		IExecutable method = (IExecutable) fIntInvariant.get(0);
		assertEquals("fInt$invar$1", method.getSimpleName());
		
	}
	
	@Test
	public void testContractCreation() {
		
		List<? extends IElement> addMethods = Elements.filter("add$*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(6, addMethods.size());
	}

	@Test
	public void testOrginalMethodIsAbstract() {
		List<? extends IElement> addMethod = Elements.filter("add", IExecutable.class, fType.getEnclosedElements());
		assertTrue(!addMethod.isEmpty());
		
		IExecutable method = (IExecutable) addMethod.get(0);
		assertTrue(method.getModifiers().contains(Modifier.ABSTRACT));
	}

	@Test
	public void testMethodAddPre() {
		List<? extends IElement> addPre = Elements.filter("add$pre$*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(2, addPre.size());
		
		for (IElement element : addPre) {
			assertTrue(element instanceof IContractExecutable);
		
			IContractExecutable tmp = (IContractExecutable) element;
			assertTrue(tmp.getModifiers().contains(Modifier.PUBLIC));
			assertNotNull(tmp.getContract());
			assertTrue(tmp.getContract() instanceof IRevealableAnnotationValue);
		}
		
		IContractExecutable pre1 = (IContractExecutable) addPre.get(0);
		assertEquals("try { return obj != null; } catch(Exception _specerror) { throw new jass.modern.core.SpecificationError(_specerror); }", pre1.getCode());
		IAnnotation metaAnno = Elements.filter(ContractInfo.class.getName(), IAnnotation.class, pre1.getEnclosedElements()).get(0);
		assertNotNull(metaAnno);
		assertEquals("do not add >null<", Elements.getValue(metaAnno, "message", String.class));
		assertEquals("obj != null", Elements.getValue(metaAnno, "code", String.class));
		
		IContractExecutable pre2 = (IContractExecutable) addPre.get(1);
		assertEquals("try { return !isFull(); } catch(Exception _specerror) { throw new jass.modern.core.SpecificationError(_specerror); }", pre2.getCode());
	}

	@Test
	public void testMethodAddPost() {
		
		List<? extends IElement> addPost = Elements.filter("add$post$*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(1, addPost.size());
	
		IContractExecutable post = (IContractExecutable) addPost.get(0);
		assertEquals(post.getParameters().size() - 2, fMethodAdd.getParameters().size());
		IAnnotation metaAnno = Elements.filter(
				ContractInfo.class.getName(), IAnnotation.class, post.getEnclosedElements()).get(0);
		assertNotNull(metaAnno);
		assertEquals("bar", Elements.getValue(metaAnno, "message", String.class));
		
		
		IVariable returnParam = getParameter(post).get(1);
		assertNotNull(returnParam);
		assertEquals("java.lang.Void", returnParam.getType());
		assertEquals("_Return", returnParam.getSimpleName());
	}

	@Test
	public void testMethodAddSignals() {
		List<? extends IElement> addPost = Elements.filter("add$signals$*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(3, addPost.size());
		
		IContractExecutable post = Elements.filter("add$signals$1", IContractExecutable.class, fType.getEnclosedElements()).get(0);
		assertEquals(post.getParameters().size() - 2, fMethodAdd.getParameters().size());
		IAnnotation metaAnno = Elements.filter(ContractInfo.class.getName(), IAnnotation.class, post.getEnclosedElements()).get(0);
		assertNotNull(metaAnno);
		assertEquals("foo", Elements.getValue(metaAnno, "message", String.class));
		
		IVariable returnParam = getParameter(post).get(1);
		assertNotNull(returnParam);
		assertEquals(Throwable.class.getName(), returnParam.getType());
		assertEquals("_Signal", returnParam.getSimpleName());
	}

	@Test
	public void testStaticMain() {
		List<? extends IElement> mainPre = Elements.filter("main$pre$*", IExecutable.class, fType.getEnclosedElements());
		assertEquals(1, mainPre.size());
		
		IContractExecutable pre = (IContractExecutable) mainPre.get(0);
		assertEquals(1, pre.getParameters().size());
		
		assertTrue(pre.getModifiers().contains(Modifier.STATIC));
	}
	
	private List<IVariable> getParameter(IExecutable exe) {
		List<? extends IElement> param = Elements.filter(IVariable.class, exe.getEnclosedElements());
		List<IVariable> vars = new ArrayList<IVariable>(param.size());
		
		for (IElement element : param) {
			vars.add((IVariable) element);
		}
		
		return vars;
	}

}

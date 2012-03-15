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
import jass.modern.core.compile.creation.ContractCreationController;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.AbstractTestUsingAnnotationProcessor;
import jass.modern.core.util.Elements;
import jass.modern.tests.Helper;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InvariantCreatorTest extends AbstractTestUsingAnnotationProcessor {

	IType type;
	
	public InvariantCreatorTest() {
		super(InvariantType.class);
	}
		
	@Before
	public void setUp() throws IOException {
		type = new TypeFactory(false).createType(Helper.openInputStream(InvariantType.class));
		assertNotNull(type);
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testCreate() {
		IAnnotation annotation = Elements.filterFirst(IAnnotation.class, type.getEnclosedElements());
		assertNotNull(annotation);
		
//		ContractCreationController.getInstance().register(new InvariantContractCreator());
		type.accept(new ContractCreationController(), fDiagnostics);
		
		List<IExecutable> list = Elements.filter("InvariantType*", IExecutable.class, type.getEnclosedElements());
		assertEquals(list.size(), 3);
	}

}

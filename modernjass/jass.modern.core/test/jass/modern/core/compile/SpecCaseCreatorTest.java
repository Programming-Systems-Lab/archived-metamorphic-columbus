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
import jass.modern.core.util.AbstractTestUsingAnnotationProcessor;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.junit.Test;

public class SpecCaseCreatorTest extends AbstractTestUsingAnnotationProcessor {

	public SpecCaseCreatorTest() {
		super(WrongVisiblityUsage.class);
	}
	
	@Test
	public void testDiagnosticsMessage() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = fDiagnostics.getDiagnostics();
		assertEquals(1, diagnostics.size());
		
		Diagnostic<? extends JavaFileObject> msg = diagnostics.get(0);
		assertEquals(Kind.ERROR, msg.getKind());
		
		System.out.println(msg);
	}

}

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
package jass.modern.core.apt;

import static org.junit.Assert.assertEquals;
import jass.modern.tests.Helper;
import jass.modern.tests.RuntimeDummyBufferWithErrors;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.Test;

public class AnnotationProcessorTest {

	DiagnosticCollector<JavaFileObject> fDiagnostics = new DiagnosticCollector<JavaFileObject>();
	private javax.tools.JavaCompiler fCompiler;
	private StandardJavaFileManager fFileManager;
	private Iterable<? extends JavaFileObject> fCu;
	private javax.tools.JavaCompiler.CompilationTask fTask;
	
	@Before
	public void runAnnotationProcessor(){
		fCompiler = ToolProvider.getSystemJavaCompiler();
		fFileManager = fCompiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		fCu = fFileManager.getJavaFileObjectsFromStrings( Arrays.asList(Helper.getAbsoluteFilePath(RuntimeDummyBufferWithErrors.class)));
		
		fTask = fCompiler.getTask(null, fFileManager, fDiagnostics, Arrays.asList("-Xlint", "-verbose", "-Averbose"), null, fCu);
		fTask.setProcessors(Arrays.asList(new AnnotationProcessor()));
		fTask.call();
	}
	
	@Test
	public void testDiagnosis() {
		assertEquals(3, fDiagnostics.getDiagnostics().size());
	}
	
	@Test
	public void printDiagnosis() {
		List<Diagnostic<? extends JavaFileObject>> diagnostics = fDiagnostics.getDiagnostics();
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
			System.out.println(diagnostic);
		}
	}
}

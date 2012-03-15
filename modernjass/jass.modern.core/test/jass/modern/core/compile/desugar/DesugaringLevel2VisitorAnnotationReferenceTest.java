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
package jass.modern.core.compile.desugar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jass.modern.core.apt.AnnotationProcessor;
import jass.modern.tests.DesugaringSample;
import jass.modern.tests.Helper;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler.CompilationTask;

import org.junit.Before;
import org.junit.Test;

public class DesugaringLevel2VisitorAnnotationReferenceTest {
	
	DiagnosticCollector<JavaFileObject> fDiagnostics = new DiagnosticCollector<JavaFileObject>();
	JavaCompiler fCompiler;
	StandardJavaFileManager fFileManager;
	Iterable<? extends JavaFileObject> fCu;
	
	private final Class<?>[] types = new Class[] { DesugaringSample.class };
	
	@Before
	public void runAnnotationProcessor(){
		fCompiler = ToolProvider.getSystemJavaCompiler();
		fFileManager = fCompiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		fCu = fFileManager.getJavaFileObjectsFromStrings(Arrays.asList(Helper.getAbsoluteFilePath(types)));
		
		CompilationTask task = fCompiler.getTask(null, fFileManager, fDiagnostics, null, null, fCu);
		task.setProcessors(Arrays.asList(new AnnotationProcessor()));
		task.call();
	}
	
	
	@Test
	public void testDiagnostic() {
		assertTrue(!fDiagnostics.getDiagnostics().isEmpty());
		
		List<Diagnostic<? extends JavaFileObject>> diagnostics = fDiagnostics.getDiagnostics();
		assertEquals(2, diagnostics.size());
	
		int note = 0, error = 0;
		
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
			if(diagnostic.getKind() == Kind.NOTE)
				note += 1;
			
			if(diagnostic.getKind() == Kind.ERROR)
				error += 1;
		}
		
		assertEquals(1, note);
		assertEquals(1, error);
	}
}

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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import jass.modern.tests.Helper;
import jass.modern.tests.RuntimeDummyBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.JavaCompiler.CompilationTask;

import org.junit.Before;
import org.junit.Test;

public class ContractJavaCompilerTest {

	ContractJavaCompiler fCompiler = ContractJavaCompiler.getInstance();
	byte[] fData;
	Class<RuntimeDummyBuffer> fClass = RuntimeDummyBuffer.class;
	DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
	
	@Before
	public void reset() {
		collector = new DiagnosticCollector<JavaFileObject>();
	}
	
	@Before
	public void setUp() throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		InputStream in = Helper.openInputStream(fClass);
		int len;
		byte[] block = new byte[1024];
		while ((len = in.read(block)) != -1)
			buffer.write(block, 0, len);

		fData = buffer.toByteArray();
	}

	@Test
	public void testGetInstance() {
		assertTrue(fCompiler == ContractJavaCompiler.getInstance());
	}

	@Test
	public void getCompilationTaskFromBytes() throws IOException {
		CompilationTask task = fCompiler.getCompilationTask(fData, collector);
		assertNotNull(task);
		
		List<Diagnostic<? extends JavaFileObject>> list = collector.getDiagnostics();
		for (Diagnostic<? extends JavaFileObject> diagnostic : list) {
			System.out.println(diagnostic.getMessage(null));
		}
		
		boolean success = task.call();
		assertTrue(success);
	}
	
	public static void main(String[] args) {
		ContractJavaCompilerTest test = new ContractJavaCompilerTest();
		try {
			test.reset();
			test.setUp();
			test.getCompilationTaskFromBytes();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			System.out.println("DONE");
		}
	}
}

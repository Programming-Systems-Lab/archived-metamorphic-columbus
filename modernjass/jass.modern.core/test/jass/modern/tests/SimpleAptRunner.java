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
package jass.modern.tests;

import jass.modern.core.apt.AnnotationProcessor;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;


public class SimpleAptRunner {
	
	public static void main(String[] args) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		
		Iterable<? extends JavaFileObject> cu = fileManager.getJavaFileObjectsFromStrings(
				Arrays.asList(Helper.getAbsoluteFilePath(RuntimeDummyBufferWithErrors.class)));
		CompilationTask task = compiler.getTask(null, fileManager, null, null, null, cu);
		
		task.setProcessors(Arrays.asList(new AnnotationProcessor()));
		task.call();
	}
}

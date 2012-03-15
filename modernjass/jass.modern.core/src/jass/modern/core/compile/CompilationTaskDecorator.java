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

import jass.modern.core.model.IType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler.CompilationTask;

public class CompilationTaskDecorator implements IExtendedCompilationTask {

	private List<IType> fTypes;
	
	private CompilationTask fTask;

	private boolean fErrors;
	
	public CompilationTaskDecorator(CompilationTask task, boolean errors, IType... types) {
		fTypes = Arrays.asList(types);
		fTask = task;
		fErrors = errors;
	}
	
	public List<IType> getTypes() {
		return fTypes;
	}

	public void setProcessors(Iterable<? extends Processor> processors) {
		fTask.setProcessors(processors);
	}

	@Override
	public Boolean call() {
		if(fErrors)
			return false;
		
		return fTask.call();
	}

	@Override
	public void setLocale(Locale locale) {
		fTask.setLocale(locale);
	}

}

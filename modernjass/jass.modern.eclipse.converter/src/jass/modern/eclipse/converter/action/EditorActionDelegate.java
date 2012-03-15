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
package jass.modern.eclipse.converter.action;

import jass.modern.eclipse.converter.refactor.JMLRefactoring;
import jass.modern.eclipse.converter.refactor.JMLRefactoringWizard;

import java.io.File;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class EditorActionDelegate implements IEditorActionDelegate {

	private ASTParser fParser = ASTParser.newParser(AST.JLS3);
	
	private ICompilationUnit fCompilationUnit;
	
	private Shell shell;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		shell = targetEditor.getEditorSite().getWorkbenchWindow().getShell();
		
		IJavaElement javaElement = JavaUI.getEditorInputJavaElement(targetEditor.getEditorInput());
		if (!(javaElement instanceof ICompilationUnit)) {
			fCompilationUnit = null;
			
		} else {
			fCompilationUnit = (ICompilationUnit) javaElement;
			fParser.setSource(fCompilationUnit);
		}
		
		// (2) do some classpath things...
		updateJassClasspath(javaElement);
	}

	private void updateJassClasspath(IJavaElement javaElement) {
		try {
			String plainClasspath = "";
			IJavaProject project = javaElement.getJavaProject();
			String offset = project.getProject().getLocation().removeLastSegments(1).toOSString() + File.separator;
			
			IClasspathEntry[] entries = project.getRawClasspath();
			for (IClasspathEntry entry : entries) {
				
				if(entry.getEntryKind() == IClasspathEntry.CPE_SOURCE || 
						entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					
					plainClasspath += offset + entry.getPath().toOSString() + File.pathSeparator;
				}
			}
			plainClasspath += offset + project.getOutputLocation().lastSegment();
			
			System.setProperty("jass.class.path", plainClasspath);
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public void run(IAction action) {
		JMLRefactoring refactoring = new JMLRefactoring(fCompilationUnit);
		JMLRefactoringWizard wizard = new JMLRefactoringWizard(refactoring, 
				RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
		
		try {
			new RefactoringWizardOpenOperation(wizard).run(shell, "Convert JML...");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}

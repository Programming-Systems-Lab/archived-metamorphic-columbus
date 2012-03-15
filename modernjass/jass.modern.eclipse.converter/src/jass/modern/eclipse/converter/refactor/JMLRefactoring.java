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
package jass.modern.eclipse.converter.refactor;

import jass.jmljass.parser.JMLjassCompilationUnit;
import jass.jmljass.parser.JMLjassParser;
import jass.jmljass.parser.ParseException;
import jass.jmljass.reflect.ClassPool;
import jass.jmljass.reflect.ClassReflection;
import jass.jmljass.reflect.TypeAnalyzer;
import jass.jmljass.visitor.ReflectVisitor;
import jass.modern.eclipse.converter.JMLjassConverter;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.TextEdit;

public class JMLRefactoring extends Refactoring {
	
	/*
	 * seejass.jmljass.reflect.TypeAnalyzer.init()
	 */
    protected static final String JASS_CLASSPATH_PROPERTY = "jass.class.path";
    protected static final String JML_SPECS_PROPERTY = "jml.specs.path";
	
	static {
		System.setProperty("jml.specs.path", "/Users/riejo/Development/Tools/JML/specs");
	}
	
	private ASTParser fParser = ASTParser.newParser(AST.JLS3);
	
	private ICompilationUnit fSource;
	private JMLjassCompilationUnit fJMLjassSource;

	private TextEdit fTextEdit;
	
	public JMLRefactoring(ICompilationUnit unit) {
		fSource = unit;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		pm.beginTask("Checking preconditions for JML import...", 1);
		
		pm.subTask("Cheching environment variables");
		if(System.getProperty(JASS_CLASSPATH_PROPERTY) == null ||
				System.getProperty(JML_SPECS_PROPERTY) == null) {
			
			return RefactoringStatus.createFatalErrorStatus("Environment variables '" + 
				JASS_CLASSPATH_PROPERTY + "' and '" + JML_SPECS_PROPERTY + 
				"' must be set.");
		}
		pm.done();
		
		return new RefactoringStatus();
	}

	private JMLjassCompilationUnit parse(InputStream in) throws ParseException {
		JMLjassParser parser = new JMLjassParser(in);
		ClassPool.initialize(/* parser */);
		TypeAnalyzer.init();

		return parser.CompilationUnit();
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		pm.beginTask("Checking final conditions", 4);
		pm.subTask("Open resource for compilation unit " + fSource.getElementName());
		IResource resource = fSource.getResource();
		if (!(resource instanceof IFile))
			return RefactoringStatus.createErrorStatus("Invalid resource for compilation unit " + fSource);
		pm.worked(1);
		
		pm.subTask("Parsing JML content in compilation unit...");
		try {
			fJMLjassSource = parse(((IFile) resource).getContents());
			
		} catch (ParseException e) {
			return RefactoringStatus.createFatalErrorStatus(
					"Parsing of " + fSource + " failed. Cause: " + e.getLocalizedMessage());
		}
		pm.worked(2);
		
		
		pm.subTask("Gathering type information...");
		ReflectVisitor refVisitor = new jass.jmljass.visitor.ReflectVisitor();
		ClassReflection clazz = (jass.jmljass.reflect.ClassReflection) fJMLjassSource.jjtAccept(refVisitor, null);
		clazz.reflect();
		pm.worked(3);
		
		pm.subTask("Computing changes...");
		fParser.setSource(fSource);
		ASTNode node = fParser.createAST(pm);
		fTextEdit = JMLjassConverter.convert(clazz, (CompilationUnit) node, pm);
		pm.worked(4);
		pm.done();
		
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		pm.beginTask("Create change...", 1);
		
		TextFileChange change = new TextFileChange(fSource.getElementName(), (IFile) fSource.getResource());
		change.setEdit(fTextEdit);
		change.setTextType("java");
		
		pm.done();
		
		return change;
	}

	@Override
	public String getName() {
		return "Convert JML";
	}

}

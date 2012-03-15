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
/**
 * 
 */
package jass.modern.eclipse.converter;

import jass.jmljass.reflect.ClassReflection;
import jass.jmljass.reflect.ModifierSet;
import jass.jmljass.reflect.jml.InvariantClause;
import jass.jmljass.reflect.jml.JMLClause;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class JMLjassConverter extends ASTVisitor {
	
	public interface ICommentNodeRequestor {
		
		public Comment getComment(int line);
	}
	
	private AST ast;
	private ASTRewrite fASTRewriter;
	private ClassReflection fClassReflection;
	private ImportRewrite fImportRewrite;
	private ICommentNodeRequestor fCommentRequestor;
	
	@SuppressWarnings("unchecked")
	public static TextEdit convert(ClassReflection clazz, final CompilationUnit unit, 
			IProgressMonitor pm) throws MalformedTreeException, CoreException {
		
		JMLjassConverter converter = new JMLjassConverter(clazz, unit, new ICommentNodeRequestor() {

			final List<Comment> comments = unit.getCommentList();
			
			public Comment getComment(int line) {
				for (Comment comment : comments) {
					int l = unit.getLineNumber(comment.getStartPosition());
					if(l == line) {
						return comment;
					}
				}
				
				return null;
			}
		});
		
		unit.accept(converter);
		
		TextEdit textEdit = new MultiTextEdit();
		textEdit.addChild(converter.fImportRewrite.rewriteImports(pm));
		textEdit.addChild(converter.fASTRewriter.rewriteAST());
		
		return textEdit;
	}

	protected JMLjassConverter(ClassReflection clazz, CompilationUnit unit, ICommentNodeRequestor requestor) {
		fClassReflection = clazz;
		ast = unit.getAST();
		fASTRewriter = ASTRewrite.create(ast);
		fImportRewrite = ImportRewrite.create(unit, true);
		fCommentRequestor = requestor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endVisit(TypeDeclaration node) {
		InvariantClause[] invariants = fClassReflection.getInvariantClauses();
		if(invariants.length == 0)
			return;
		
		TypeDeclaration copy = (TypeDeclaration) ASTNode.copySubtree(ast, node);
		
		if(invariants.length == 1) {
			InvariantClause invariant = invariants[0];
			NormalAnnotation annotation = createInvariantAnnotation(invariant);
			removeJMLClause(invariant);
			
			copy.modifiers().add(0, annotation);
			
		} else {
			List<NormalAnnotation> annotations = new ArrayList<NormalAnnotation>(invariants.length);
			for (InvariantClause invariantClause : invariants) {
				annotations.add(createInvariantAnnotation(invariantClause));
			}
			
			SingleMemberAnnotation annotation = createInvariantWrapperAnnotation(annotations);
			copy.modifiers().add(0, annotation);
		}
		
		fASTRewriter.replace(node, copy, null);
	}

	@SuppressWarnings("unchecked")
	protected SingleMemberAnnotation createInvariantWrapperAnnotation(List<NormalAnnotation> annotations) {
		
		ArrayInitializer value = ast.newArrayInitializer();
		for (NormalAnnotation normalAnnotation : annotations) {
			value.expressions().add(normalAnnotation);
		}

		SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
		annotation.setTypeName(ast.newName("InvariantDefinitions"));
		annotation.setValue(value);
		fImportRewrite.addImport("jass.modern.InvariantDefinitions");
		
		return annotation;
	}

	@SuppressWarnings("unchecked")
	protected NormalAnnotation createInvariantAnnotation(InvariantClause invariant) {
		
		// (1) set the expression
		MemberValuePair assertion = ast.newMemberValuePair();
		assertion.setName(ast.newSimpleName("value"));
		StringLiteral expression = ast.newStringLiteral();
		expression.setLiteralValue(invariant.getPredicate().toString());
		assertion.setValue(expression);
		
		// (2) set the visibility
		String modifier = ModifierSet.modifiersToString(invariant.getModifiers());
		modifier = modifier.length() == 0 ? "PACKAGE_PRIVATE" : modifier.toUpperCase();
		MemberValuePair visibility = ast.newMemberValuePair();
		visibility.setName(ast.newSimpleName("visibility"));
		visibility.setValue(ast.newQualifiedName(
				ast.newSimpleName("Visibility"),
				ast.newSimpleName(modifier)));
		fImportRewrite.addImport("jass.modern.Visibility");
		
		// (3) create the annotation...
		NormalAnnotation annotation = ast.newNormalAnnotation();
		annotation.setTypeName(ast.newName("Invariant"));
		annotation.values().add(assertion);
		annotation.values().add(visibility);
		fImportRewrite.addImport("jass.modern.Invariant");
		
		return annotation;
	}

	private void removeJMLClause(JMLClause invariant) {
		Comment comment = fCommentRequestor.getComment(invariant.getCodeLine());
		if(comment != null) {
			fASTRewriter.remove(comment, null);
		}
	}
}

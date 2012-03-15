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
package jass.modern.eclipse.ui.decorator;

import jass.modern.eclipse.ModernJassPlugin;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

public class ContractDecorator extends LabelProvider implements
		ILightweightLabelDecorator {
	
	private final class AnnotationsVisitor extends ASTVisitor {
		
		private boolean fTargetAnnotationFound = false;
		
		public boolean isTargetAnnotationFound() {
			return fTargetAnnotationFound;
		}
			
		@Override
		public boolean visit(NormalAnnotation node) {
			return visitAnnotation(node);
		}

		@Override
		public boolean visit(MarkerAnnotation node) {
			return visitAnnotation(node);
		}

		@Override
		public boolean visit(SingleMemberAnnotation node) {
			return visitAnnotation(node);
		}

		private boolean visitAnnotation(Annotation node) {
			
			fTargetAnnotationFound |= ANNOTATION_NAMES.contains(
					node.getTypeName().getFullyQualifiedName());
			
			return true;
		}
	}
	
	final static List<String> ANNOTATION_NAMES = Arrays.asList(
			"Invar", 
			"Pre", 
			"Post", 
			"Also",
			"SpecCase", 
			"NonNull", 
			"Min", 
			"Max", 
			"Range",
			"Helper", 
			"Pure",
			"Length", 
			"Model", 
			"ModelDefinitions",
			"Represents", 
			"RepresentsDefinitions");
	
	private final ImageDescriptor fImageTwo = ModernJassPlugin.imageDescriptorFromPlugin("icons/dbc_ov-b.gif");
	
	public void decorate(Object element, IDecoration decoration) {
		if(! (element instanceof IResource)) 
			return;
		
		IResource objectResource = (IResource) element;

		if (objectResource.getType() != IResource.FILE)
			return;

		IJavaElement javaElement = JavaCore.create(objectResource);
		if (javaElement == null)
			return;

		if (javaElement.getElementType() != IJavaElement.COMPILATION_UNIT)
			return;
		
		
		ICompilationUnit cu = (ICompilationUnit)javaElement;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		ASTNode node = parser.createAST(null);
		AnnotationsVisitor visitor = new AnnotationsVisitor();
		node.accept(visitor);
		
		if(!visitor.isTargetAnnotationFound())
			return;
	
		decoration.addOverlay(fImageTwo, IDecoration.TOP_RIGHT);
	}
}

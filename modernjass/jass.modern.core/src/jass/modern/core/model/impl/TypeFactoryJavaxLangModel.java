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
package jass.modern.core.model.impl;

import jass.modern.core.apt.IRevealableAnnotationValue;
import jass.modern.core.apt.RevealableAnnotationValue;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.IType.Kind;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;

class TypeFactoryJavaxLangModel {

	public final class InternalElementVisitor extends ElementScanner6<Void, IElement>{
		
		private final Void _VOID = null;
		private Type fType;
		
		@Override
		public Void visitType(TypeElement e, IElement p) {
			
			// (1) create type
			Type newType = new Type(e.getQualifiedName().toString(), convert(e.getKind()));
			newType.fModifier.addAll(Modifier.convert(e.getModifiers()));
			
			// (2) set superclass and interfaces
			newType.setSuperclass(toString(e.getSuperclass()));
			List<? extends TypeMirror> interfaces = e.getInterfaces();
			for (TypeMirror typeMirror : interfaces) {
				newType.addInterface(toString(typeMirror));
			}
			
			// (3) process generic signature
			List<? extends TypeParameterElement> genericTypes = e.getTypeParameters();
			for (TypeParameterElement type : genericTypes) {
				newType.addGenericSignature(toString(type));
			}
			scan(e, e.getAnnotationMirrors(), newType);
			scan(e.getEnclosedElements(), newType);
			
			if(fType == null)
				fType = newType;
			else
				fType.addEnclosedElement(newType);
			
			return _VOID;
		}

		@Override
		public Void visitVariable(VariableElement e, IElement parent) {
			
			Variable variable = new Variable(e.getSimpleName().toString(), toString(e.asType()));
			variable.fModifier.addAll(Modifier.convert(e.getModifiers()));
			scan(e, e.getAnnotationMirrors(), variable);
			
			if(e.asType() instanceof DeclaredType) {
				List<? extends TypeMirror> genericTypes = ((DeclaredType) 
						e.asType()).getTypeArguments();
				
				for (TypeMirror typeMirror : genericTypes) {
					variable.addGenericSignature(toString(typeMirror));
				}
			}
			
			parent.addEnclosedElement(variable);			
			return _VOID;
		}

		@Override
		public Void visitExecutable(ExecutableElement e, IElement parent) {
								
			jass.modern.core.model.impl.ExecutableElement element = null; 
			
			// (1) create element - decide if constructor or not
			if(e.getSimpleName().toString().equals("<init>")) {
				element = new jass.modern.core.model.impl.ExecutableElement(
					parent.getSimpleName(), null);
				element.setConsutructor(true);
				
			} else {
				element = new jass.modern.core.model.impl.ExecutableElement(
						e.getSimpleName().toString(), toString(e.getReturnType()));
			}
			element.fModifier.addAll(Modifier.convert(e.getModifiers()));
			
			// (2) add generic signature...
			List<? extends TypeParameterElement> genericTypes = e.getTypeParameters();
			for (TypeParameterElement type : genericTypes) {
				element.addGenericSignature(toString(type));
			}
			
			// (3) add parameters
			scan(e.getParameters(), element);
			
			// (4) add throws list
			for(TypeMirror typeMirror : e.getThrownTypes()) {
				element.addException(toString(typeMirror));
			}
					
			// (5) add annotations
			scan(e, e.getAnnotationMirrors(), element);
			
			parent.addEnclosedElement(element);
			return _VOID;
		}

		public void visitAnnotation(javax.lang.model.element.Element parent, AnnotationMirror annotation, IElement p) {
			Annotation tmp = new Annotation(toString(annotation.getAnnotationType()));
			
			for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
				annotation.getElementValues().entrySet()) {
				
				IAnnotationValue value = new RevealableAnnotationValue(
						entry.getKey().getSimpleName().toString(),
						parent, annotation, entry.getValue());
				
				entry.getValue().accept(new InternalAnnotationValueVisitor(), (IRevealableAnnotationValue) value);
				tmp.addEnclosedElement(value);
			}
			
			p.addEnclosedElement(tmp);
		}

		public IType getType() {
			return fType;
		}

		private Kind convert(ElementKind kind) {
			switch(kind) {
			case ANNOTATION_TYPE:	return Kind.ANNOTATION;
			case INTERFACE:	return Kind.INTERFACE;
			case ENUM:		return Kind.ENUM;
			case CLASS:		return Kind.CLASS;
			default: return null;
			}
		}
		
		private void scan(javax.lang.model.element.Element parent, 
				List<? extends AnnotationMirror> annotations, IElement p) {
			
			for (AnnotationMirror annotationMirror : annotations) {
				visitAnnotation(parent, annotationMirror, p);
			}
		}
		
		/*
		 * TODO this method needs an improvement
		 * since it relies on the TypeMirror.toString
		 * method where I dont know how accurate it
		 * works.
		 */
		private String toString(TypeMirror typeMirror) {
			
			TypeKind kind = typeMirror.getKind();
			switch(kind) {
			case VOID:
				return "void";
				
			case ARRAY:
			default:
				if (typeMirror instanceof DeclaredType) {
					DeclaredType tmp = (DeclaredType) typeMirror;
					return ((TypeElement) tmp.asElement()).getQualifiedName().toString();
					
				} else {
					return typeMirror.toString();
				}
			}
		}
		
		private String toString(TypeParameterElement element) {
			String str = element.getSimpleName().toString();
			List<? extends TypeMirror> bounds = element.getBounds();
			if(bounds.isEmpty() || 
					bounds.get(0).toString().equals("java.lang.Object")) {
				
				return str;
			}
			
			return str + " extends " + bounds.get(0);
		}
	}
	
	private final class InternalAnnotationValueVisitor
			extends	SimpleAnnotationValueVisitor6<Void, IRevealableAnnotationValue> {
		
		@Override
		protected Void defaultAction(Object o, IRevealableAnnotationValue p) {
			
			/*
			 * An annotation attribute of the type java.lang.Class is represented
			 * as DeclaredType and hence must be handled appropriatly.
			 */
			if (o instanceof DeclaredType) {
				String className = ((DeclaredType) o).asElement().toString();
				Class<?> cls = ContractJavaCompiler.getInstance().forName(className);
				p.getValue().add(cls);
				
			} else if(o instanceof ArrayType) { 
//				String className = ((ArrayType) o).getComponentType().asElement().toString();
				Class<?> cls = ContractJavaCompiler.getInstance().forName(o.toString());
				p.getValue().add(cls);
				
			}else {
				p.getValue().add(o);
			}
			
			return null;
		}
	
		@Override
		public Void visitAnnotation(AnnotationMirror a,	IRevealableAnnotationValue p) {
			new InternalElementVisitor().visitAnnotation(p.getElement(), a, (Element) p);
			return null;
		}
	
		@Override
		public Void visitArray(List<? extends AnnotationValue> vals, IRevealableAnnotationValue p) {
			for (AnnotationValue annotationValue : vals) {
				annotationValue.accept(new InternalAnnotationValueVisitor(), p);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void visitEnumConstant(VariableElement c, IRevealableAnnotationValue p) {
			Class<? extends Enum> cls = (Class<? extends Enum>) ContractJavaCompiler.getInstance().
				forName((c.asType().toString()));
				
			p.setValue(Enum.valueOf(cls, c.getSimpleName().toString()));
			return null;
		}
	}

	TypeFactoryJavaxLangModel() {
		
	}
	
	public IType createTypeFromTypeElement(TypeElement element) {
		
		InternalElementVisitor visitor = new InternalElementVisitor();
		element.accept(visitor, null);
		
		return visitor.getType();
	}
}

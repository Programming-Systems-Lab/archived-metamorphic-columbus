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

import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationReference;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.IType.Kind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

public final class TypeFactoryComSunMirror {
	
	public interface IComSunMirrorRevealable extends IAnnotationReference {

		public abstract SourcePosition getSourcePosition();

	}
	
	public final class ComSunMirrorRevealableAnnotation extends Annotation 
		implements IComSunMirrorRevealable {

		private SourcePosition fSourcePosition;
		
		public ComSunMirrorRevealableAnnotation(String name, SourcePosition pos) {
			super(name);
			fSourcePosition = pos;
		}
		
		public ComSunMirrorRevealableAnnotation(ComSunMirrorRevealableAnnotation other) {
			super(other);
			fSourcePosition = other.fSourcePosition;
		}
		
		public SourcePosition getSourcePosition() {
			return fSourcePosition;
		}
		
		public Object clone() {
			return new ComSunMirrorRevealableAnnotation(this);
		}
	}
	
	public final class ComSunMirrorRevealableAnnotationValue extends AnnotationValue 
		implements IComSunMirrorRevealable {

		private SourcePosition fSourcePosition;
		
		public ComSunMirrorRevealableAnnotationValue(String name, SourcePosition pos) {
			super(name);
			fSourcePosition = pos;
		}
		
		public ComSunMirrorRevealableAnnotationValue(ComSunMirrorRevealableAnnotationValue other) {
			super(other);
			fSourcePosition = other.fSourcePosition;
		}
		
		public SourcePosition getSourcePosition() {
			return fSourcePosition;
		}
		
		public Object clone() {
			return new ComSunMirrorRevealableAnnotationValue(this);
		}
	}
	
	public final class InteralDeclarationVisitor extends SimpleDeclarationVisitor {

		private IType fType;
		private Deque<Type> newTypes = new LinkedList<Type>();
		private Type newType;
		private ContractJavaCompiler fContractCompiler = ContractJavaCompiler.getInstance();
		

		protected void scan(Collection<? extends Declaration> declarations) {
			for (Declaration declaration : declarations) {
				declaration.accept(this);
			}
		}
		
		@Override
		public void visitTypeDeclaration(TypeDeclaration decl) {
			newType = new Type(decl.getQualifiedName(), convert(decl));
			newTypes.push(newType);
			newType.fModifier.addAll(convert(decl.getModifiers()));
			
			// (1) superclass
			if (decl instanceof ClassDeclaration) {
				ClassType superType = ((ClassDeclaration) decl).getSuperclass();
				newType.setSuperclass(superType != null ? 
						superType.getDeclaration().getQualifiedName() : null);
				
			} else if(! newType.getQualifiedName().equals(Type.JAVA_LANG_OBJECT)){
				newType.setSuperclass(Type.JAVA_LANG_OBJECT);
			}
			
			// (2) interfaces
			Collection<InterfaceType> interfaces = decl.getSuperinterfaces();
			for (InterfaceType _interface : interfaces) {
				newType.addInterface(_interface.getDeclaration().getQualifiedName());
			}
			
			// (3) generics
			Collection<TypeParameterDeclaration> typeParameters = decl.getFormalTypeParameters();
			for (TypeParameterDeclaration typeParameter : typeParameters) {
				newType.addGenericSignature(toString(typeParameter));
			}
			
			// (4) annotations
			addAnnotations(decl.getAnnotationMirrors(), newType);
			
			// (5) fields and methods
			scan(decl.getFields());
			if (decl instanceof ClassDeclaration) {
				scan( ((ClassDeclaration) decl).getConstructors());
			}
			scan(decl.getMethods());
			
			if(fType == null) {
				fType = newType;
				newTypes.pop();
				
			} else {
				fType.addEnclosedElement(newType);
				newTypes.pop();
			}
		}
		
		@Override
		public void visitFieldDeclaration(FieldDeclaration decl) {
			Variable variable = new Variable(decl.getSimpleName(), decl.getType().toString());
			variable.fModifier.addAll(convert(decl.getModifiers()));
			addAnnotations(decl.getAnnotationMirrors(), variable);		
			
			newType.addEnclosedElement(variable);
		}

		@Override
		public void visitConstructorDeclaration(ConstructorDeclaration decl) {
			visitExecutableDeclaration(decl);
		}

		@Override
		public void visitExecutableDeclaration(ExecutableDeclaration decl) {
			ExecutableElement method = null;
			
			// (1) create element
			if (decl instanceof ConstructorDeclaration) {
				method = new ExecutableElement(newType.getSimpleName(), null);
				method.setConsutructor(true);
			} else {
				method = new ExecutableElement(decl.getSimpleName(), 
						((MethodDeclaration) decl).getReturnType().toString());
			}
			method.fModifier.addAll(convert(decl.getModifiers()));
			
			// (2) generics
			Collection<TypeParameterDeclaration> typeParameters = decl.getFormalTypeParameters();
			for (TypeParameterDeclaration typeParameter : typeParameters) {
				method.addGenericSignature(toString(typeParameter));
			}
			
			// (3) parameter
			Collection<ParameterDeclaration> parameters = decl.getParameters();
			for (ParameterDeclaration parameter : parameters) {
				Variable tmp = new Variable(parameter.getSimpleName(), 
						parameter.getType().toString());
				addAnnotations(parameter.getAnnotationMirrors(), tmp);
				method.addParameter(tmp);
			}
			
			// (4) throws...
			Collection<ReferenceType> throwsList = decl.getThrownTypes();
			for (ReferenceType referenceType : throwsList) {
				method.addException(((DeclaredType)referenceType).
						getDeclaration().getQualifiedName());
			}
			
			// (5) annotations
			addAnnotations(decl.getAnnotationMirrors(), method);
			
			// (6) add to parent
			newType.addEnclosedElement(method);
		}

		public IType getType() {
			if(!newTypes.isEmpty())
				throw new IllegalStateException();
			
			return fType;
		}

		private void addAnnotations(Collection<AnnotationMirror> annotations, IElement element) {
			
			for (AnnotationMirror annotation : annotations) {
				element.addEnclosedElement(
						toIAnnotation(annotation) );
			}
		}
		
		private IAnnotation toIAnnotation(AnnotationMirror annotation) {
			Annotation jassAnnotation = new ComSunMirrorRevealableAnnotation(annotation.getAnnotationType().
					getDeclaration().getQualifiedName(), annotation.getPosition());
			
			Set<Entry<AnnotationTypeElementDeclaration, com.sun.mirror.declaration.AnnotationValue>> entries = 
				annotation.getElementValues().entrySet();
			
			for (Entry<AnnotationTypeElementDeclaration, 
					com.sun.mirror.declaration.AnnotationValue> entry : entries) {
				
				IAnnotationValue jassValue = new ComSunMirrorRevealableAnnotationValue(
						entry.getKey().getSimpleName(), entry.getValue().getPosition());
				
				jassValue.setValue(toValues(entry.getValue().getValue()));
				jassAnnotation.addEnclosedElement(jassValue);
			}
			
			return jassAnnotation;
		}

		@SuppressWarnings("unchecked")
		private Object[] toValues(Object value) {
			if(value instanceof ArrayType){
				Class<?> cls = fContractCompiler.forName(value.toString());
				return new Object[] { cls };
				
			} else if (value instanceof TypeMirror) {
				TypeMirror typeMirror = (TypeMirror) value;
				String name = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
				Class<?> cls = fContractCompiler.forName(name);
				return new Object[] { cls };
				
			}else if (value instanceof AnnotationMirror) {
				return new Object[] {toIAnnotation( (AnnotationMirror) value) };
			
			} else if (value instanceof Collection) {
				List<Object> tmp = new LinkedList<Object>();
				for (com.sun.mirror.declaration.AnnotationValue v : 
					(Collection<com.sun.mirror.declaration.AnnotationValue>) value) {
					
					tmp.addAll(Arrays.asList(toValues(v.getValue())));
				}
				return tmp.toArray();
			
			} else if(value instanceof EnumConstantDeclaration) {
				EnumConstantDeclaration tmp = (EnumConstantDeclaration) value;
				Class<? extends Enum> cls = (Class<? extends Enum>) 
					fContractCompiler.forName(tmp.getDeclaringType().getQualifiedName());
				
				return new Object[] { Enum.valueOf(cls, tmp.toString()) } ;
				
			} else {
			
				return new Object[] { value };
			}	
		}	
		
		private Kind convert(TypeDeclaration decl) {
			
			if(decl instanceof AnnotationTypeDeclaration) {
				return Kind.ANNOTATION;
				
			} else if(decl instanceof InterfaceType) {
				return Kind.INTERFACE;
				
			} else if(decl instanceof EnumType) {
				return Kind.ENUM;
				
			} else if (decl instanceof ClassDeclaration) {
				return Kind.CLASS;
			}
			
			return null;
		}
		
		private Collection<Modifier> convert(Collection<com.sun.mirror.declaration.Modifier> modifier){
			List<Modifier> list = new ArrayList<Modifier>(modifier.size());
			
			for (com.sun.mirror.declaration.Modifier modifier2 : modifier) {
				switch (modifier2) {
				case ABSTRACT:
					list.add(Modifier.ABSTRACT);
					break;

				case FINAL:
					list.add(Modifier.FINAL);
					break;
					
				case NATIVE:
					list.add(Modifier.NATIVE);
					break;
					
				case PRIVATE:
					list.add(Modifier.PRIVATE);
					break;
					
				case PROTECTED:
					list.add(Modifier.PROTECTED);
					break;
					
				case PUBLIC:
					list.add(Modifier.PUBLIC);
					break;
					
				case STATIC:
					list.add(Modifier.STATIC);
					break;
					
				case SYNCHRONIZED:
					list.add(Modifier.SYNCHRONIZED);
					break;
					
				}
			}
			
			if( !(list.contains(Modifier.PUBLIC) || list.contains(Modifier.PROTECTED) 
					|| list.contains(Modifier.PRIVATE))) {
				
				list.add(Modifier.PACKAGE_PRIVATE);
			}
			
			return list;
		}
		
		private String toString(TypeParameterDeclaration typeParameter) {
			
			StringBuilder buffer = new StringBuilder();
			for(ReferenceType bound : typeParameter.getBounds()) {
				DeclaredType type = (DeclaredType) bound;
				buffer.append(type.getDeclaration().getDeclaringType().getQualifiedName());
				buffer.append('&');
			}
			
			if(buffer.length() == 0)
				return typeParameter.getSimpleName();
			
			buffer.deleteCharAt(buffer.length() - 1);
			return typeParameter.getSimpleName() + " extends " + buffer.toString();
		}
		
	}
	
	TypeFactoryComSunMirror() {
		/* only package visiblity */
	}
	
	IType createTypeFromTypeDeclaration(TypeDeclaration type) {
		
		InteralDeclarationVisitor visitor = new InteralDeclarationVisitor();
		type.accept(visitor);
		
		return visitor.getType();
	}
}

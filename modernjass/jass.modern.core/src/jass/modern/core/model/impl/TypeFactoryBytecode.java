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

import jass.modern.Name;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.IType.Kind;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceSignatureVisitor;

/*package private*/ class TypeFactoryBytecode {
	
	static final class InternalClassVisitor extends EmptyVisitor {
		
		private static final Pattern PATTERN = Pattern.compile(
				"(((\\w+\\.)*)[a-zA-Z][\\w\\$]*(\\<[a-zA-Z\\p{javaWhitespace}0-9 \\_\\,]*\\>)?)");
		
		IType fType;

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			
			// (1) create type
			final Type newType = new Type(name, convert(access));
			newType.fModifier.addAll(Modifier.flag(access));
			
			boolean interfaceDone = false;
			boolean superTypeDone = false;
			
			// (2) generic signature, maybe supertypes & interfaces
			if(signature != null) {
				TraceSignatureVisitor v = new TraceSignatureVisitor(access);
				new SignatureReader(signature).accept(v);
				String declaration = v.getDeclaration();
				
				// (2.1) add defined generics
				int start = declaration.indexOf('<') + 1;
				int end = declaration.indexOf('>');
				assert start < end;
				String tmp = declaration.substring(start, end);
				for(String decl : tmp.split(",")) {
					newType.addGenericSignature(decl.trim());
				}
				
				// (2.2) interfaces
				int _implements = declaration.lastIndexOf("implements");
				if(_implements != -1) {
					tmp = declaration.substring(_implements + "implements".length());
					Matcher matcher = PATTERN.matcher(tmp);
					while(matcher.find()) {
						newType.addInterface(matcher.group(1).trim());
					}					interfaceDone = true;
				}
				
				// (2.3) super type
				int _extends = declaration.lastIndexOf("extends");
				if(_extends != -1) {
					start = _extends + "extends".length();
					end = _implements != -1 ? _implements - 1 : declaration.length();
					tmp = declaration.substring(start, end);
					newType.setSuperclass(tmp.trim());
					superTypeDone = true;
				}
				
			}
			
			// (3) superclass and interfaces
			if(!superTypeDone && superName != null) {
				newType.setSuperclass(superName.replace('/', '.'));
			}
			if(!interfaceDone) {
				for (String _interface : interfaces) {
					newType.addInterface(_interface.replace('/', '.'));
				}
			}
			
			// (4) add to nesting type or leave it
			if (fType == null) {
				fType = newType;
				
			} else {
				fType.addEnclosedElement(newType);
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			
			return new InternalAnnotationVisitor(fType, 
					TypeFactoryBytecode.classDescriptorToClassName(desc));
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
		
			final Variable variable = new Variable(name, TypeFactoryBytecode.classDescriptorToClassName(desc));
			variable.fModifier.addAll(Modifier.flag(access));
			fType.addEnclosedElement(variable);
			
			if(signature != null) {
				TraceSignatureVisitor visitor = new TraceSignatureVisitor(access);
				new SignatureReader(signature).accept(visitor);
				
				String types = visitor.getDeclaration();
				int start = types.indexOf('<');
				int end = types.lastIndexOf('>');
				
				if(start != -1 && end != -1) {
					types = types.substring(start + 1, end);
					for(String type : types.split(",")) {
						variable.addGenericSignature(type.trim());
					}
				}
			}
			
			return new EmptyVisitor() {

				@Override
				public AnnotationVisitor visitAnnotation(String desc,
						boolean visible) {
					
					return new InternalAnnotationVisitor(variable, 
							TypeFactoryBytecode.classDescriptorToClassName(desc));
				}
				
			};
		}

		@Override
		public MethodVisitor visitMethod(int access, final String name,
				final String desc, final String signature,
				final String[] exceptions) {
			
			if(name.equals("<clinit>"))
				return null;

			boolean isStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;

			TraceSignatureVisitor visitor = null;
			if(signature != null) {
				visitor = new TraceSignatureVisitor(access);
				new SignatureReader(signature).accept(visitor);
			}

			String returnType = visitor != null ? visitor.getReturnType() : 
				org.objectweb.asm.Type.getReturnType(desc).getClassName();
			
			// (1) create method or constructor
			final ExecutableElement element;
			if(name.equals("<init>")) {
				element = new ExecutableElement(fType.getSimpleName(), null);
				element.setConsutructor(true);
				
			} else {
				element = new ExecutableElement(name, returnType);
			}
			element.fModifier.addAll(Modifier.flag(access));
			element.fModifier.remove(Modifier.VOLATILE);
			element.fModifier.remove(Modifier.TRANSIENT);
			
			fType.addEnclosedElement(element);
			
			if(visitor != null && visitor.getDeclaration() != null) {
				String types = visitor.getDeclaration();
				int end = types.indexOf('>');
				
				if(end >= 0 && end < types.indexOf('(')) {
					types = types.substring(1, end);
					for(String type : types.split(",")) {
						element.addGenericSignature(type.trim());
					}
				}
			}
			
			// (2) create throws-list
			if (exceptions != null) {
				for (String exception : exceptions) {
					element.addException(exception.replace('/', '.'));
				}
			}

			// (3) create list of arguments (infer names!), (4) visit annotations
			final List<org.objectweb.asm.Type> parameters = Arrays.asList(
					org.objectweb.asm.Type.getArgumentTypes(desc));
			
			String[] genericTypes = null;
			if(visitor != null && visitor.getDeclaration() != null) {
				String decl = visitor.getDeclaration();
				int start = decl.indexOf('(');
				int end = decl.lastIndexOf(')');
				
				decl = decl.substring(start + 1, end);
				genericTypes = decl.split(",");
			}
			
			return new InternalMethodVisitor(element, parameters, genericTypes, isStatic);
		}
		
		@Override
		public void visitInnerClass(String name, String outerName,
				String innerName, int access) {
			
			// (1) skip anonymous inner classes
			if(innerName == null)
				return;
			
			if(!fType.getQualifiedName().equals(outerName.replace('/', '.')))
				return;
			
//			if((access & Opcodes.ACC_PRIVATE) != 0)
//				return;
			
			try {
				IType innerType = new TypeFactory(false).createType(name);
				fType.addEnclosedElement(innerType);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public IType getType() {
			return fType;
		}
		
		public Kind convert(int access) {
			if((access & Opcodes.ACC_ANNOTATION) != 0)
				return Kind.ANNOTATION;
			
			else if((access & Opcodes.ACC_INTERFACE) != 0)
				return Kind.INTERFACE;

			else if((access & Opcodes.ACC_ENUM) != 0)
				return Kind.ENUM;
			else
				return Kind.CLASS;
		}
	}

	static final class InternalAnnotationVisitor implements AnnotationVisitor {

		IElement fAnnotation;
		IElement fBackupElement;
		
		InternalAnnotationVisitor(IElement parent, String name) {
			fAnnotation = new Annotation(name);
			parent.addEnclosedElement(fAnnotation);
		}
		
		InternalAnnotationVisitor(IAnnotation annotation) {
			fAnnotation = annotation;
		}
		
		public void visit(String name, Object value) {
			
			/*
			 * An attribute value of type java.lang.Class
			 * is represented as asm.Type at this place.
			 * Therefore a conversion is required.
			 */
			if (value instanceof org.objectweb.asm.Type) {
				String className = ((org.objectweb.asm.Type) value).getClassName();
				Class<?> cls = ContractJavaCompiler.getInstance().forName(className);
				value = cls;
			}
			
			new AnnotationValue(
					(IAnnotation) fAnnotation, name, value);
		}

		public AnnotationVisitor visitAnnotation(String name, String desc) {
			name = org.objectweb.asm.Type.getType(desc).getClassName().replace('$', '.');
			return new InternalAnnotationVisitor(fAnnotation, name);
		}

		public AnnotationVisitor visitArray(String name) {
			IAnnotationValue tmp = new AnnotationValue(
					(IAnnotation) fAnnotation, name);

			fBackupElement = fAnnotation;
			fAnnotation = tmp;
			return this;
		}

		@SuppressWarnings("unchecked")
		public void visitEnum(String name, String desc, String value) {
			String clsName = org.objectweb.asm.Type.getType(desc).getClassName();
			Class<? extends Enum> cls = (Class<? extends Enum>) ContractJavaCompiler.getInstance().forName(clsName);
			visit(name, Enum.valueOf(cls, value));
		}

		public void visitEnd() {
			if(fBackupElement != null && fAnnotation instanceof IAnnotationValue)
				fAnnotation = fBackupElement;
		}

		public IElement getAnnotation() {
			return fAnnotation;
		}
	}

	/**
	 * Resolution of parameter names works in three steps
	 * <ol>
	 * <li>Check for the &#064;Name-annotation.
	 * <li>Read parameter names from debug info
	 * <li>Assign generic names ala <code>param<i>N</i></code>
	 * 	where <code><i>N</i></code> is an integer starting
	 * 	at <code>1</code>.
	 * </ol>
	 *
	 * @author riejo
	 */
	static final class InternalMethodVisitor extends EmptyVisitor {
		
		static final String SPEC_NAME = org.objectweb.asm.Type.getDescriptor(Name.class);
		
		IExecutable fElement;
		boolean fIsStatic;
		List<org.objectweb.asm.Type> fParameters;
		String[] fGenericType;
		
		Map<Integer, List<IAnnotation>> fParameterAnnotations = new HashMap<Integer, List<IAnnotation>>();
		List<Integer> fParameterWhiteList = new LinkedList<Integer>();
		
		InternalMethodVisitor(IExecutable element, List<org.objectweb.asm.Type> parameters, 
				String[] genericTypes, boolean isStatic) {
			
			fElement = element;
			fParameters = parameters;
			fGenericType = genericTypes;
			fIsStatic = isStatic;
		}
		
		/**
		 * Visit a method annotation. 
		 * @return Returns a new instance of {@link InternalAnnotationVisitor}
		 * 	which parent is {@link #fElement}.
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			
			return new InternalAnnotationVisitor(fElement, 
					TypeFactoryBytecode.classDescriptorToClassName(desc));
		}
		
		/**
		 * Visit the annotation of parameters. If the annotation is not
		 * the {@link Name} annotation they are relfected in the resulting
		 * model. The {@link Name} annotation is used to derive the name
		 * of the parameter only (see {@link #visitSpecNameAnnotation(int)}).
		 */
		@Override
		public AnnotationVisitor visitParameterAnnotation(final int parameter,
				String desc, boolean visible) {
			
			if(desc.equals(SPEC_NAME)) {
				return visitSpecNameAnnotation(parameter);
			
			} else {
			
				String name = TypeFactoryBytecode.classDescriptorToClassName(desc);
				IAnnotation annotation = new Annotation(name);
				ensureKey(parameter).add(annotation);
				
				return new InternalAnnotationVisitor(annotation);
			}
		}

		protected AnnotationVisitor visitSpecNameAnnotation(final int index) {
			
			if(fParameterWhiteList.contains(index))
				return null;
			
			return new EmptyVisitor() {
				@Override
				public void visit(String name, Object value) {
					org.objectweb.asm.Type param = fParameters.get(index);
					String type = getGenericType(index);
					
					IVariable var = new Variable((String) value, type != null ?
							type : TypeFactoryBytecode.classDescriptorToClassName(param.getDescriptor()));
					
					addAnnotations(index, var);
					fElement.addParameter(var);
					fParameterWhiteList.add(index);
				}
			};
		}
		
		/**
		 * This method only called when the compiler has be called with
		 * the <code>-g</code> option.
		 */
		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			
			index = fIsStatic ? index : index - 1;
			org.objectweb.asm.Type parameterType = org.objectweb.asm.Type.getType(desc);
			if (!fParameterWhiteList.contains(index) && fParameters.contains(parameterType)) {
			
				String type = getGenericType(index);
				IVariable var = new Variable(name, type != null ? 
						type : TypeFactoryBytecode.classDescriptorToClassName(desc));
				
				addAnnotations(index, var);
				fElement.addParameter(var);
			}
		}

		/**
		 * If parameter could not be resolved, have generic names
		 */
		@Override
		public void visitEnd() {
			if(fElement.getParameters().size() == fParameters.size())
				return;
			
			// (1) remove existent parameter
			for (IVariable parameter : fElement.getParameters()) {
				fElement.removeEnclosedElement(parameter);
			}
			
			// (2) add new parameter with generic names
			int i = 1;
			for (org.objectweb.asm.Type parameter : fParameters) {

				String type = getGenericType(i-1);
				type = type != null ? type : parameter.getClassName();
				fElement.addParameter(new Variable("param" + i++, type));
			}
		}
		
		private void addAnnotations(int index, IVariable var) {
			List<IAnnotation> parameterAnnotations = fParameterAnnotations.get(index);
			if(parameterAnnotations == null)
				return;
			
			for (IAnnotation annotation : parameterAnnotations) {
				var.addEnclosedElement(annotation);
			}
		}
		
		private List<IAnnotation> ensureKey(int key) {
			List<IAnnotation> list = fParameterAnnotations.get(key);
			if(list == null) {
				list = new LinkedList<IAnnotation>();
				fParameterAnnotations.put(key, list);
			}
			
			return list;
		}
		
		private String getGenericType(int index) {
			if(fGenericType == null || index < 0 || index >= fGenericType.length)
				return null;
			
			return fGenericType[index].trim();
		}
	}

	
	/*package private*/ TypeFactoryBytecode() {	}

	public IType createTypeFrom(InputStream in) throws IOException {

		InternalClassVisitor visitor = new InternalClassVisitor();
		ClassReader reader = new ClassReader(in);
		reader.accept(visitor, 0);

		return visitor.getType();
	}
	
	public IType createTypeByName(String fqName) throws IOException {
		InternalClassVisitor visitor = new InternalClassVisitor();
		try {
			ClassReader reader = new ClassReader(fqName);
			reader.accept(visitor, 0);
		
		} catch(IOException e) {
			throw new IOException("Could not read class >>" + fqName + "<<", e);
		}
		
		return visitor.getType();
	}

	private static String classDescriptorToClassName(String desc) {
		return org.objectweb.asm.Type.getType(desc).getClassName();
	}
}

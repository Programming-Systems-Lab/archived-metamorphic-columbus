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

import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationReference;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IContractElement;
import jass.modern.core.model.IContractExecutable;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IParameterized;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.TypeFactory;
import jass.modern.core.util.Elements;
import jass.modern.core.util.EmptyElementVisitor;
import jass.modern.core.util.Primitive;
import jass.modern.meta.ContractInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.SimpleJavaFileObject;

public class ContractJavaFile extends SimpleJavaFileObject {
	
	private class ContractPrinter extends EmptyElementVisitor<StringBuilder> {
		
		private boolean fRootClass = true;
		
		private long fCurrentLine = 1;

		protected void appendLine(String str) {
			appendLine(str, (IAnnotationReference) null);
		}
		
		protected void appendLine(String str, IContractElement element) {
			if(element.getContract() instanceof IAnnotationReference)
				appendLine(str, (IAnnotationReference) element.getContract());
			else
				appendLine(str);
		}
		
		protected void appendLine(String str, IAnnotationReference element) {
			boolean linefeed = str.endsWith("\r") || str.endsWith("\n");
			try {
				ContractJavaFile.this.fBuffer.write(str.getBytes());
			} catch (IOException e) {			}
			
			if(!linefeed)
				ContractJavaFile.this.fBuffer.write('\n');
			
			if(element != null)
				ContractJavaFile.this.fLineNumberMap.put(fCurrentLine, element);
			
			fCurrentLine += 1;
		}

		private String toString(List<String> list) {
			StringBuilder buffer = new StringBuilder();
			for (String str : list) {
				buffer.append(str );
				buffer.append(',');
			}
			return removeLastChar(buffer).toString();
		}
		
		private String toString(IParameterized parameterized) {
			if(parameterized.getGenericSignature().isEmpty())
				return "";
			
			return "<" + toString(parameterized.getGenericSignature()) + ">";
		}

		private String toString(Set<Modifier> modifier) {
			List<Modifier> list = new LinkedList<Modifier>(modifier);
			Collections.sort(list);
			StringBuilder builder = new StringBuilder();
			for (Modifier mod : list) {
				builder.append(mod.toString() + " ");
			}
			
			return removeLastChar(builder).toString();
		}
		
		private String toString(IExecutable executable) {

			String modifierStr = toString(executable.getModifiers());
			
			StringBuilder formalParameter = new StringBuilder();
			scan(executable.getParameters(), formalParameter);
			
			String throwsStr = toString(executable.getExceptions());
			throwsStr = throwsStr.length() == 0 ? "" : "throws " + throwsStr;
			
			String genericStr = toString( (IParameterized) executable);
			
			if(executable.isConstructor()) {
				
				StringBuilder buffer= new StringBuilder();
				buffer.append('\t');
				buffer.append(modifierStr);
				buffer.append(genericStr);
				buffer.append(' ');
				buffer.append(executable.getEnclosingElement().getSimpleName());
				buffer.append('(');
				buffer.append(removeLastChar(formalParameter));
				buffer.append(')');
				buffer.append(throwsStr);
				buffer.append('{');
				IType type = Elements.getParent(IType.class, executable);
				IType parent;
				try {
					parent = new TypeFactory(true).createType(type.getSuperclass());
					List<IExecutable> list = getNonDefaultConstructors(parent);
					if(!list.isEmpty()) {
						buffer.append("super");
						buffer.append('(');
						
						IExecutable constructor = list.get(0);
						int count= constructor.getParameters().size();
						for (IVariable parameter : constructor.getParameters()) {
							appendDefaultValue(buffer, parameter);
							if(--count > 0)
								buffer.append(", ");
						}
						
						buffer.append(')');
						buffer.append(';');
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				buffer.append('}');
				return buffer.toString();
				
			} else {
				
				return "\t" + modifierStr + genericStr + " " + 
					executable.getReturnType() + " " + executable.getSimpleName() + 
					"(" + removeLastChar(formalParameter) + ")" + throwsStr;
				
			}
		}

		private void appendDefaultValue(StringBuilder buffer, IVariable parameter) {
			String type = parameter.getType();
			Primitive primitive = Primitive.parseString(type);
			if(primitive == null) {
				buffer.append("null");
				
			} else {
				switch (primitive) {
					case BOOLEAN:
						buffer.append("false");
						break;
						
					case BYTE:
					case SHORT:
					case CHAR:
					case INT:
					case LONG:
					case FLOAT:
					case DOUBLE:
						buffer.append("0");
						break;
		
					default:
						buffer.append("null");
						break;
				}
			} 
		}
		
		private List<IExecutable> getNonDefaultConstructors(IType type) {
			if(type == null)
				return Collections.emptyList();
			
			List<IExecutable> list= Elements.filter(type.getSimpleName(), IExecutable.class, type.getEnclosedElements());
			Iterator<IExecutable> iter = list.iterator();
			while(iter.hasNext()) {
				if(iter.next().getParameters().size() == 0)
					iter.remove();
			}
			
			return list;
		}
		
		private StringBuilder removeLastChar(StringBuilder builder) {
			int len = builder.length();
			if(builder == null || len == 0)
				return builder;
			
			builder.deleteCharAt(len-1);
			return builder;
		}
		
		@Override
		public void visit(IVariable element, StringBuilder param) {
			String str = element.getType() + toString(element) + " " + element.getSimpleName();
			if(param == null) {
				/*
				 * XXX trick
				 * Remove the final keyword because
				 * the modern jass model does not 
				 * support values.
				 */
				element.removeModifier(Modifier.FINAL);
				appendLine("\t" + toString(element.getModifiers()) + " " + str + ";");
				
			} else {
				param.append(toString(element.getModifiers()) + " " + str+",");
			}
		}

		
		@Override
		public void visit(IAnnotation element, StringBuilder param) {
			List<? extends IElement> values = Elements.filter(
					IAnnotationValue.class, element.getEnclosedElements());
			
			// (1) skip all contract annotations...
			if(element.getSimpleName().matches("jass\\.modern\\.[A-Z]\\w+"))
				return;
			
			// (1) print none-contract-annotation
			StringBuilder buffer = new StringBuilder();
			for(IElement value : values) {
				IAnnotationValue tmp = (IAnnotationValue) value;
				buffer.append(value.getSimpleName() + "=\"" + Elements.getValue(tmp, String.class) + "\",");
			}
			buffer.deleteCharAt(buffer.length() - 1);
			
			appendLine("\t@" + element.getSimpleName() + "(" + buffer.toString() + ")");
		}

		@Override
		public void visit(IContractExecutable element, StringBuilder param) {
			
			scan(Elements.filter(ContractInfo.class.getName(), 
					IAnnotation.class, element.getEnclosedElements()), param);
			
			String str = toString((IExecutable) element) + 
				"{ " + element.getCode() +" }";
			
			appendLine(str, element);
		}
		
		@Override
		public void visit(IExecutable element, StringBuilder param) {
			
			/*
			 * XXX trick
			 * do not repeat the declaration 
			 * of an inherited method
			 */
			if(!element.isConstructor() && 
					Elements.overrides((IType) element.getEnclosingElement(), element)) {
				
				return;
			}
			
			element.removeModifier(Modifier.PRIVATE);
			element.removeModifier(Modifier.STATIC);
			
			if(!element.isConstructor())
				element.addModifier(Modifier.ABSTRACT);
			
			String str = toString(element) + ";";
			appendLine(str);
		}

		@Override
		public void visit(IType element, StringBuilder param) {
			
//			if(!hasEmptyConstructor(element)) {
//				addEmptyConstructor(element);
//			}
			
			int len = -1 + element.getQualifiedName().length() - element.getSimpleName().length();
			String packageStr = element.getQualifiedName().substring(0, len);
			
			String interfaceStr = toString(element.getInterfaces());
			interfaceStr = interfaceStr.length() == 0 ? "" : "implements " + interfaceStr;
			
			String fPrintName = element.getSimpleName();
			fPrintName = fRootClass ? fPrintName : fPrintName.substring(fPrintName.lastIndexOf('$') + 1);

			if(fRootClass) {
				appendLine("package " + packageStr + ";");
				fRootClass = false;
			}
			
			appendLine("public abstract class " + fPrintName + toString(element) + 
					" extends " + element.getSuperclass() + " " + interfaceStr + "{");
			scan(element.getEnclosedElements(), param);
			appendLine("}");
		}
		
//		boolean hasEmptyConstructor(IType type) {
//			List<IExecutable> methods = Elements.filter(IExecutable.class, type.getEnclosedElements());
//			for (IExecutable executable : methods) {
//				if(executable.isConstructor() && 
//						executable.getParameters().size() == 0) {
//				
//					return true;
//				}
//			}
//			
//			return false;
//		}
//		
//		void addEmptyConstructor(IType type) {
//			IExecutable tmp = new ExecutableElement(type.getSimpleName(), null);
//			tmp.setConsutructor(true);
//			
//			type.addEnclosedElement(tmp);
//		}
	}
	
	private IType fType;
	
	private ByteArrayOutputStream fBuffer = new ByteArrayOutputStream();
	
	private Map<Long, IAnnotationReference> fLineNumberMap = new HashMap<Long, IAnnotationReference>();
	
	/**
	 * The {@link #getKind() kind} of this contract file
	 * is <code>source</code>.
	 * 
	 * @param type
	 */
	public ContractJavaFile(IType type) {
		super(Elements.toURI(type, Kind.SOURCE), Kind.SOURCE);
		
		fType = type;
		type.accept(new ContractPrinter(), null);
	}
	
	/**
	 * The {@link #getKind() kind} of this contract file
	 * is <code>class</code>.
	 * 
	 * @param name
	 */
	public ContractJavaFile(String name) {
		super(Elements.toURI(name, Kind.CLASS), Kind.CLASS);
		
		fType = null;
		fBuffer = new ByteArrayOutputStream();
	}
	
	
	/**
	 * 
	 * @return Returns the {@link IType} associated with
	 * 	this contract file. If the kind of this file is 
	 *  {@link Kind#CLASS} no type exists and 
	 *  <code>null</code> is returned.
	 */
	public IType getType() {
		return fType;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new BufferedInputStream(new ByteArrayInputStream(fBuffer.toByteArray()));
	}
	
	
	@Override
	public OutputStream openOutputStream() throws IOException {
		fBuffer.reset();
		return new BufferedOutputStream(fBuffer);
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		
		return new String(fBuffer.toByteArray());
	}
	
	public IAnnotationReference getAnnotationReference(long line) {
		return fLineNumberMap.get(line);
	}

	@Override
	public boolean delete() {
		fBuffer.reset();
		return true;
	}
}

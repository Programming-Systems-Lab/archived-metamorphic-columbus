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
package jass.modern.core.bytecode.contracts;

import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import jass.modern.core.model.IType;
import jass.modern.core.model.impl.TypeFactory;

import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.util.CheckMethodAdapter;

/**
 * The class visitor hosts a {@link InnerMethodAdapter method visitor}
 * which transforms bytecode from contract-methods so that
 * they can be executed in their new environment.
 * <br />
 * <br />
 * Java bytecode differs between calling the method
 * of a class <code>INVOKE_VIRTUAL</code> or of an 
 * interface <code>INVOKE_INTERFACE</code>. Because
 * all contract methods get compiled in the context
 * of an abstract class this differentiation gets
 * lost. Hence, the contract code cleaner looks out
 * for method invocations of interfaces which do not
 * use the <code>INVOKE_INTERFACE</code> opcode. Its
 * resolution is to change the owner of the method 
 * call to the current type.
 *
 * @author riejo
 */
public class ContractCodeCleaner extends ClassAdapter {
	
	
	private class InnerMethodAdapter extends GeneratorAdapter {

		public InnerMethodAdapter(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
		}

//		@Override
//		public void visitMethodInsn(int opcode, String owner, String name,
//				String desc) {
//			
//			boolean model = name.endsWith("$model");
//			boolean rewire = Opcodes.INVOKEVIRTUAL == opcode && 
//				isSuperType(owner) && isInterface(owner);
//			
//			if(model) {
//				super.visitMethodInsn(opcode, fClassName, name, desc);
//				
//			} else if(rewire) {
//				int[] indexes = storeArguments(Type.getArgumentTypes(desc));
//				visitTypeInsn(Opcodes.CHECKCAST, fClassName);
//				loadArguments(indexes);
//				super.visitMethodInsn(opcode, fClassName, name, desc);
//				
//			} else {
//				super.visitMethodInsn(opcode, owner, name, desc);
//			}
//		}
		
		/**
		 * If a method get's called on a type which is a super type
		 * of the current type, make sure the current type is not
		 * supposed to be the caller. 
		 * <pre>
		 * if(this == reference){
		 *   this.callMethod(...);
		 * } else {
		 *   reference.callMethod(...);
		 * }
		 * </pre>
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			
			if(name.endsWith("$model")) {
				owner = fClassName;
				super.visitMethodInsn(opcode, owner, name, desc);
				
			} else if(opcode != Opcodes.INVOKESPECIAL && opcode != Opcodes.INVOKESTATIC && 
					isSuperType(owner) && isInterface(owner)){
				
				Type thisType = Type.getType("L" + fClassName + ";");
				Type ownerType = Type.getType("L" + owner + ";");
				
				Label notThis = new Label();
				Label postInvoke = new Label();
				int[] arguments = storeArguments(Type.getArgumentTypes(desc));
				
				visitInsn(Opcodes.DUP);
				visitVarInsn(Opcodes.ALOAD, 0);
				visitJumpInsn(Opcodes.IF_ACMPNE, notThis);	// if(this == reference) 
				
				visitTypeInsn(Opcodes.CHECKCAST, thisType.getInternalName());
				loadArguments(arguments);
				super.visitMethodInsn(opcode, fClassName, name, desc);
				
				visitJumpInsn(Opcodes.GOTO, postInvoke);	// else
				visitLabel(notThis);
				
				visitTypeInsn(Opcodes.CHECKCAST, ownerType.getInternalName());
				loadArguments(arguments);
				super.visitMethodInsn(opcode, owner, name, desc);
				
				visitLabel(postInvoke);			// fi
				
			} else {
				super.visitMethodInsn(opcode, owner, name, desc);
			}
		}

		protected void loadArguments(int[] indexes) {
			for (int i = indexes.length - 1; i >= 0; i--) {
				loadLocal(indexes[i]);
			}
		}

		protected int[] storeArguments(Type[] types) {
			int[] indexes = new int[types.length];
		
			for (int i = types.length - 1; i >= 0; i--) {
				int local = newLocal(types[i]);
				storeLocal(local);
				
				indexes[i] = local;
			}
			return indexes;
		}
	}

	private String fClassName;
	
	private List<String> fHierarchy;
	
	private TypeFactory fTypeFactory = new TypeFactory(true);
	
	public ContractCodeCleaner(ClassVisitor cv, String className, List<String> hierarchy) {
		super(cv);
		
		fClassName = className;
		fHierarchy = hierarchy;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access | ACC_SYNTHETIC, 
				name, desc, signature, exceptions);
		
		return new InnerMethodAdapter(new CheckMethodAdapter(mv), access, name, desc);
	}

	protected boolean isSuperType(String type) {
		return fHierarchy.indexOf(type.replace('/', '.')) > 0;
	}
	
	protected boolean isInterface(String owner) {
		
		owner = owner.replace('/', '.');
		try {
			IType type = fTypeFactory.createType(owner);
			return type.getKind().ordinal() >= IType.Kind.INTERFACE.ordinal();
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

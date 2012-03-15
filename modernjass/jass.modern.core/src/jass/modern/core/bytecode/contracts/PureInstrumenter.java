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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PureInstrumenter implements ClassFileTransformer {
	
	private final class InternalClassAdapter extends ClassAdapter {

		public InternalClassAdapter(ClassVisitor cv) {
			super(cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			return new InternalMethodAdapter(mv);
		}
		
		
		
	}
	
	private final class InternalMethodAdapter extends MethodAdapter {

		public InternalMethodAdapter(MethodVisitor mv) {
			super(mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
		 		String desc) {
			
			super.visitMethodInsn(Opcodes.INVOKESTATIC, 
					"java/lang/Thread", 
					"currentThread", 
					"()Ljava/lang/Thread;");
			super.visitLdcInsn(owner);
			super.visitLdcInsn(name);
			super.visitMethodInsn(Opcodes.INVOKESTATIC, 
					"jass/modern/core/runtime/PureWatcher", 
					"pendingFieldChange", 
					"(Ljava/lang/Thread;Ljava/lang/String;Ljava/lang/String;)V");
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		reader.accept(new InternalClassAdapter(writer), 0);
		
		return writer.toByteArray();
	}

	
}

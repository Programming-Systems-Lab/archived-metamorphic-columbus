package edu.columbia.cs.psl.metamorphic.runtime.visitor;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import edu.columbia.cs.psl.metamorphic.runtime.Interceptor;

public class InterceptingClassVisitor extends ClassVisitor {
	private Logger logger = Logger.getLogger(InterceptingClassVisitor.class);
	private String className;
	public static String CLONE_OVERRIDE_METHOD = "__metamorphicClone";
	public static String IS_CHILD_FIELD = "__metamorphicIsChild";
	public InterceptingClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature,
				exceptions);
		InterceptingMethodVisitor imv = new InterceptingMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
		imv.setClassName(className);
		imv.setClassVisitor(this);
		return imv;
	}
	private boolean willRewrite = false;
	public void setShouldRewrite()
	{
		willRewrite = true;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		if(willRewrite)
		{
			FieldNode fn = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PRIVATE,
					InterceptingMethodVisitor.INTERCEPTOR_FIELD_NAME,
					Type.getDescriptor(Interceptor.class), null, null); //TODO: abstract the interceptor type
			fn.accept(cv);
			
			
			FieldNode fn3 = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
					InterceptingMethodVisitor.STATIC_INTERCEPTOR_FIELD_NAME,
					Type.getDescriptor(Interceptor.class), null, null); //TODO: abstract the interceptor type
			fn3.accept(cv);
			
			
			FieldNode fn2 = new FieldNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC,
					IS_CHILD_FIELD,
					Type.BOOLEAN_TYPE.getDescriptor(), null, false); //TODO: abstract the interceptor type
			fn2.accept(cv);
			logger.info("Actually rewrote" + className);
		}
	}

	public void setClassName(String name) {
		this.className = name;
	}
}

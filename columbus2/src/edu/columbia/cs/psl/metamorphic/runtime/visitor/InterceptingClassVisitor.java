package edu.columbia.cs.psl.metamorphic.runtime.visitor;

import java.util.ArrayList;
import java.util.HashSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import edu.columbia.cs.psl.metamorphic.runtime.Interceptor;

public class InterceptingClassVisitor extends ClassVisitor {

	private String className;
	public static String IS_CHILD_FIELD = "__metamorphicIsChild";
//	public static String RETRIEVE_RULES_METHOD = "__metamorphicRules";
	public InterceptingClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	private HashSet<InterceptingMethodVisitor> imvs = new HashSet<InterceptingMethodVisitor>();
	@Override
	public MethodVisitor visitMethod(int acc, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(acc, name, desc, signature,
				exceptions);
		InterceptingMethodVisitor imv = new InterceptingMethodVisitor(Opcodes.ASM4, mv, acc, name, desc);
		imv.setClassName(className);
		imvs.add(imv);
		return imv;
	}


	@Override
	public void visitEnd() {
		super.visitEnd();
		
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
				Type.BOOLEAN_TYPE.getDescriptor(), null, false); 
		fn2.accept(cv);
		
//		int numberOfTests = 0;
//		for(InterceptingMethodVisitor imv : imvs)
//		{
//			ArrayList<String> rules = imv.getRules();
//			if(rules != null && rules.size() > 0)
//			{
//				MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, RETRIEVE_RULES_METHOD+imv.getName()+imv.getDesc().hashCode(), "()[Ljava/lang/String;", null, null);
//				System.out.println(RETRIEVE_RULES_METHOD+imv.getName()+imv.getDesc().hashCode());
//				System.out.println(imv.getDesc());
//				mv.visitIntInsn(Opcodes.SIPUSH, rules.size());
//				mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
//				for(int i = 0;i<rules.size();i++)
//				{
//					mv.visitInsn(Opcodes.DUP);
//					mv.visitIntInsn(Opcodes.SIPUSH, i);
//					mv.visitLdcInsn(rules.get(i));
//					mv.visitInsn(Opcodes.AASTORE);
//				}
//				mv.visitInsn(Opcodes.ARETURN);
//				mv.visitMaxs(0, 0);
//				mv.visitEnd();
//			}
//		}

		

	}
	/*
    LINENUMBER 112 L0
    BIPUSH 6
    ANEWARRAY java/lang/String
    DUP
    ICONST_0
    LDC "abc"
    AASTORE
    DUP
    ICONST_1
    LDC "def"
    AASTORE
    DUP
    ICONST_2
    LDC "ghi"
    AASTORE
    DUP
    ICONST_3
    LDC "zzzz"
    AASTORE
    DUP
    ICONST_4
    LDC "dsf"
    AASTORE
    DUP
    ICONST_5
    LDC "sdfasdf"
    AASTORE
    ARETURN
	 */
	public static String[] getRules()
	{
		return new String[] {"abc","def", "ghi","zzzz","dsf","sdfasdf"};
	}
	public void setClassName(String name) {
		this.className = name;
	}
}

package edu.columbia.cs.psl.metamorphic.runtime.visitor;

import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;

import edu.columbia.cs.psl.metamorphic.runtime.Interceptor;

public class InterceptingMethodVisitor extends AdviceAdapter {
	public final static String INTERCEPTOR_CLASS_NAME = "edu/columbia/cs/psl/metamorphic/runtime/Interceptor";
	public final static String INTERCEPTOR_FIELD_NAME = "___interceptor__by_mountaindew";
	public final static String STATIC_INTERCEPTOR_FIELD_NAME = "___interceptor__by_mountaindew_static";
	private int access;

	private int api;
	private Type[] argumentTypes;
	private String className;

	private String desc;

	private String name;

	int refIdForInterceptor;

	boolean rewrite = false;

	private MetamorphicRuleAnnotationVisitor ruleNode;

	protected InterceptingMethodVisitor(int api, MethodVisitor mv, int access,
			String name, String desc) {
		super(api, mv, access, name, desc);
		this.name = name;
		this.api = api;
		this.access = access;
		this.argumentTypes = Type.getArgumentTypes(desc);
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getRules() {
		if (ruleNode == null)
			return null;
		return ruleNode.getRules();
	}

	private void onMemberMethodEnter() {
		Label the_method = new Label();
		visitIntInsn(ALOAD, 0);
		super.visitFieldInsn(GETFIELD, className.replace(".", "/"),
				INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");
		super.visitJumpInsn(IFNONNULL, the_method);
		visitIntInsn(ALOAD, 0);
		visitTypeInsn(NEW, INTERCEPTOR_CLASS_NAME);
		dup();
		loadThis();
		visitMethodInsn(INVOKESPECIAL, INTERCEPTOR_CLASS_NAME, "<init>",
				"(Ljava/lang/Object;)V");
		super.visitFieldInsn(PUTFIELD, className.replace(".", "/"),
				INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");

		visitLabel(the_method);

		refIdForInterceptor = newLocal(Type.INT_TYPE);

		visitIntInsn(ALOAD, 0);
		super.visitFieldInsn(GETFIELD, className.replace(".", "/"),
				INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");
		visitLdcInsn(name);

		push(argumentTypes.length);
		newArray(Type.getType(String.class));
		for (int i = 0; i < argumentTypes.length; i++) {
			dup();
			push(i);
			if (argumentTypes[i].getSort() != Type.OBJECT
					&& argumentTypes[i].getSort() != Type.ARRAY)
				visitLdcInsn(argumentTypes[i].getClassName());
			else
				visitLdcInsn(argumentTypes[i].getInternalName().replace("/",
						"."));
			box(Type.getType(String.class));
			arrayStore(Type.getType(String.class));
		}

		loadArgArray();
		loadThis();
		invokeVirtual(
				Type.getType(Interceptor.class),
				Method.getMethod("int __onEnter (java.lang.String, java.lang.String[], java.lang.Object[], java.lang.Object)"));
		storeLocal(refIdForInterceptor);
		super.onMethodEnter();
	}

	@Override
	protected void onMethodEnter() {
		if (!rewrite)
			return;
		if ((access & Opcodes.ACC_STATIC) != 0)
			onStaticMethodEnter();
		else
			onMemberMethodEnter();

	}

	public void onMethodExit(int opcode) {
		if (!rewrite)
			return;

		if (opcode == RETURN) {
			visitInsn(ACONST_NULL);
		} else if (opcode == ARETURN || opcode == ATHROW) {
			dup();
		} else {
			if (opcode == LRETURN || opcode == DRETURN) {
				dup2();
			} else {
				dup();
			}
			box(Type.getReturnType(this.methodDesc));
		}
		if ((access & Opcodes.ACC_STATIC) != 0) {
			super.visitFieldInsn(GETSTATIC, className.replace(".", "/"),
					STATIC_INTERCEPTOR_FIELD_NAME, "L"
							+ Interceptor.class.getName().replace(".", "/")
							+ ";");
		} else {
			visitIntInsn(ALOAD, 0);
			super.visitFieldInsn(GETFIELD, className.replace(".", "/"),
					INTERCEPTOR_FIELD_NAME, "L"
							+ Interceptor.class.getName().replace(".", "/")
							+ ";");
		}
		swap();
		visitIntInsn(SIPUSH, opcode);
		loadLocal(refIdForInterceptor);
		visitMethodInsn(INVOKEVIRTUAL, INTERCEPTOR_CLASS_NAME, "onExit",
				"(Ljava/lang/Object;II)V");
	}

	private void onStaticMethodEnter() {
		Label the_method = new Label();

		super.visitFieldInsn(GETSTATIC, className.replace(".", "/"),
				STATIC_INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");
		super.visitJumpInsn(IFNONNULL, the_method);

		// Initialize a new interceptor with the class as the intercepted object
		visitTypeInsn(NEW, INTERCEPTOR_CLASS_NAME);
		dup();
		visitLdcInsn(className);
		visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
				"(Ljava/lang/String;)Ljava/lang/Class;");
		visitMethodInsn(INVOKESPECIAL, INTERCEPTOR_CLASS_NAME, "<init>",
				"(Ljava/lang/Object;)V");
		super.visitFieldInsn(PUTSTATIC, className.replace(".", "/"),
				STATIC_INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");

		visitLabel(the_method);

		refIdForInterceptor = newLocal(Type.INT_TYPE);

		super.visitFieldInsn(GETSTATIC, className.replace(".", "/"),
				STATIC_INTERCEPTOR_FIELD_NAME, "L"
						+ Interceptor.class.getName().replace(".", "/") + ";");
		visitLdcInsn(name);

		push(argumentTypes.length);
		newArray(Type.getType(String.class));
		for (int i = 0; i < argumentTypes.length; i++) {
			dup();
			push(i);
			if (argumentTypes[i].getSort() != Type.OBJECT
					&& argumentTypes[i].getSort() != Type.ARRAY)
				visitLdcInsn(argumentTypes[i].getClassName());
			else
				visitLdcInsn(argumentTypes[i].getInternalName().replace("/",
						"."));
			box(Type.getType(String.class));
			arrayStore(Type.getType(String.class));
		}

		loadArgArray();
		visitLdcInsn(className);
		visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
				"(Ljava/lang/String;)Ljava/lang/Class;");
		invokeVirtual(
				Type.getType(Interceptor.class),
				Method.getMethod("int __onEnter (java.lang.String, java.lang.String[], java.lang.Object[], java.lang.Object)"));
		storeLocal(refIdForInterceptor);
		super.onMethodEnter();
	}

	public void setClassName(String className) {
		this.className = className;

	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc.equals("Ledu/columbia/cs/psl/metamorphic/runtime/annotation/Metamorphic;")) {
			ruleNode = new MetamorphicRuleAnnotationVisitor(api,
					super.visitAnnotation(desc, visible));
			rewrite = true;
		}
		return ruleNode;
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals);
	}

}

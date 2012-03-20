package edu.columbia.cs.psl.metamorphic.compiler;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;

public class MetamorphicPropertyCompiler {
	private static MetamorphicPropertyCompiler instance;
	
	public static MetamorphicPropertyCompiler getInstance(DiagnosticCollector<JavaFileObject> diagnostics, ProcessingEnvironment processingEnv) {
		if(instance == null)
			instance = new MetamorphicPropertyCompiler(diagnostics,processingEnv);
		return instance;
	}
	private ProcessingEnvironment processingEnv;
	
	private MetamorphicPropertyCompiler(DiagnosticCollector<JavaFileObject> diagnostics,ProcessingEnvironment processingEnv)
	{
		this.processingEnv = processingEnv;
	}
	private String toString(Set<Modifier> modifiers) {
		StringBuilder buf = new StringBuilder();
		for (Modifier mod : modifiers) {
			buf.append(mod.name() + " ");
		}
		return buf.substring(0, (buf.length() > 1 ? buf.length() - 1 : 0)).toString();
	}
	private void raiseError(String msg, ExecutableElement method, int ruleIndex, String type) {
		try {
			for (AnnotationMirror am : processingEnv.getElementUtils().getAllAnnotationMirrors(((ExecutableElement) method))) {
				if (!processingEnv.getElementUtils().getTypeElement(Metamorphic.class.getName()).equals(am.getAnnotationType().asElement()))
					continue;
				this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Metamorphic error: " + msg, method, am);
				return;
			}
		} catch (Exception ex) {
			raiseError(ex.getMessage(), method);
		}
	}

	private void raiseError(String msg, Element source) {
		this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Metamorphic error: " + msg, source);
	}
	

	public void compileTestCode(MetamorphicClassFile c) {
		c.markDone();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(c.getFile().openWriter());
		} catch (IOException e1) {
			raiseError("Unable to write file", c.getTypeElement());
		}
		try {

			// PrintStream bw = System.out;
			StringBuilder buf = new StringBuilder();
			buf.append("package ");
			buf.append(((PackageElement) c.getTypeElement().getEnclosingElement()).getQualifiedName());
			buf.append(";\n");
			buf.append("public class " + c.getTypeElement().getSimpleName() + "_tests {\n");
			for (ExecutableElement m : c.getMethods()) {
				int i = 0;
				for (Rule rule : m.getAnnotation(Metamorphic.class).rules()) {
					buf.append("@SuppressWarnings(\"all\")\npublic static ");
					buf.append(" " + m.getReturnType() + " ");
					buf.append(m.getSimpleName() + "_" + i + " (");
					StringBuilder realParams = new StringBuilder();
					
					for (VariableElement param : m.getParameters()) {
						String type = param.asType().toString();
						if (param.asType().getKind().isPrimitive()) {
							type = Constants.primitiveToObject.get(type);
						}
						realParams.append(toString(param.getModifiers()));
						realParams.append(" ");
						realParams.append(type);
						realParams.append(" ");
						realParams.append(param.getSimpleName());
						realParams.append(", ");
					}
					
					buf.append(realParams);
					buf.append(c.getTypeElement().getSimpleName() + " " + Constants.TEST_OBJECT_PARAM_NAME + ", java.lang.reflect.Method "
							+ Constants.TEST_METHOD_PARAM_NAME);
					buf.append(") throws Exception {\n");

					String formattedRule = formatRule(m.getSimpleName().toString(), rule);

					if (formattedRule == null || formattedRule.length() < 1)
						raiseError("Unparsable rule", m, i, "test");
					buf.append("return " + getCastString(m.getReturnType()) + " " + formatRule(m.getSimpleName().toString(), rule) + ";\n");
					buf.append("\n}\n");
					buf.append("@SuppressWarnings(\"all\")\npublic static ");
					buf.append(" boolean ");
					buf.append(m.getSimpleName() + "_Check" + i + " (" + m.getReturnType() + " orig, " + m.getReturnType() + " metamorphic"+ (realParams.length() > 0 ? ","+realParams.substring(0,realParams.length()-2) : "")+")");
					buf.append(" {\n");
					if (!m.getReturnType().getKind().isPrimitive())
						buf.append("if(orig == null && metamorphic != null) return false; if(orig == null && metamorphic == null) return true;");

					formattedRule = formatRuleCheck(m, rule, m.getReturnType(), i);

					if (formattedRule == null || formattedRule.length() < 1)
						raiseError("Unparsable rule", m, i, "check");

					buf.append("return " + formattedRule + "\n");
					buf.append("\n}");

					i++;
				}
			}
			buf.append("\n}");
				bw.append(buf);
		} catch (Exception ex) {
			raiseError("Unknown error " + ex.getMessage(), c.getTypeElement());
		}
		try {
			bw.close();
		} catch (IOException ex) {
			raiseError("Unable to write file", c.getTypeElement());
		}

	}


	private String getCastString(TypeMirror type) {
		if (!type.getKind().isPrimitive())
			return "(" + type + ")";
		else {
			return "("+Constants.primitiveToObject.get(type.getKind().name().toLowerCase())+")";
		}
//		return "(Object)";
	}

	private String formatRule(String methodName, Rule rule) throws Exception {
		String left = rule.test();
		left = left.replaceAll(methodName + "\\(",
				Constants.TEST_METHOD_PARAM_NAME + ".invoke(" + Constants.TEST_OBJECT_PARAM_NAME + (left.contains(methodName + "()") ? "" : ","));

		Pattern p = Pattern.compile("\\\\([^(]+)\\(");
		Matcher m = p.matcher(left);
		left = m.replaceAll("new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.$1().apply((Object) ");

		return left;
	}

	private String formatRuleCheck(ExecutableElement method, Rule rule, TypeMirror returnType, int ruleIndex) throws Exception {
		String right = rule.check();

		right = right.replace("\\result", "orig");
		if (rule.checkMethod().equals("==") || rule.checkMethod().equals(">=") || rule.checkMethod().equals("<=") || rule.checkMethod().equals("<")
				|| rule.checkMethod().equals(">") || rule.checkMethod().equals("!=")) {
			if (returnType.getKind().isPrimitive())
				right = "metamorphic " + rule.checkMethod() + " " + right + ";";
			else {
				if (rule.checkMethod().equals("!="))
					right = "! metamorphic.equals(" + right + ");";
				else if (rule.checkMethod().equals("=="))
					right = "metamorphic.equals(" + right + ");";
				else {
					if (processingEnv.getTypeUtils().isSubtype(returnType,
							processingEnv.getElementUtils().getTypeElement(Comparable.class.getName()).asType()))
						right = "metamorphic.compareTo(" + right + ") " + rule.checkMethod() + " 0;";
					else
						raiseError("Check method indicates a " + rule.checkMethod() + " check operation, but the return type (" + returnType
								+ ") is not Comparable", method, ruleIndex, "checkMethod");
				}
			}
		}
		return right;
	}


}

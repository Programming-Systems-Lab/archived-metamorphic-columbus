package edu.columbia.cs.psl.metamorphic.compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;

import javax.tools.Diagnostic;

@SupportedAnnotationTypes("edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {

	private TypeElement getEnclosingClass(Element e) {
		if (e instanceof TypeElement)
			if (((TypeElement) e).getEnclosingElement() instanceof PackageElement)
				return (TypeElement) e;
		return getEnclosingClass(e.getEnclosingElement());

	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		classFiles = new HashMap<TypeElement, AnnotatedClass>();

		for (TypeElement typeElement : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
				if (!classFiles.containsKey(getEnclosingClass(element))) {
					try {
						AnnotatedClass ac = new AnnotatedClass();
						ac.setClazz(getEnclosingClass(element));
						ac.setJfo(processingEnv.getFiler().createSourceFile(getEnclosingClass(element).getQualifiedName() + "_tests"));
						ac.setMethods(new ArrayList<ExecutableElement>());
						classFiles.put(ac.getClazz(), ac);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (element.getKind().equals(ElementKind.METHOD)) {
					// this is a method
					AnnotatedClass ac = classFiles.get(getEnclosingClass(element));
					ac.getMethods().add(((ExecutableElement) element));
				}
			}
		}

		if (annotations.size() > 0)
			generateCode();
		return true;
	}

	private boolean	errorRaised	= false;

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
		errorRaised = true;
	}

	private void raiseError(String msg, Element source) {
		this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Metamorphic error: " + msg, source);
		errorRaised = true;
	}

	private HashMap<TypeElement, AnnotatedClass>	classFiles;

	private void generateCode() {
		for (TypeElement e : classFiles.keySet()) {
			AnnotatedClass c = classFiles.get(e);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(c.getJfo().openWriter());
			} catch (IOException e1) {
				raiseError("Unable to write file", e);
			}
			try {
				errorRaised = false;

				// PrintStream bw = System.out;
				StringBuffer buf = new StringBuffer();
				buf.append("package ");
				buf.append(((PackageElement) c.getClazz().getEnclosingElement()).getQualifiedName());
				buf.append(";\n");
				buf.append("public class " + c.getClazz().getSimpleName() + "_tests {\n");
				for (ExecutableElement m : c.getMethods()) {
					int i = 0;
					for (Rule rule : m.getAnnotation(Metamorphic.class).rules()) {
						buf.append("public static ");
						buf.append(" " + m.getReturnType() + " ");
						buf.append(m.getSimpleName() + "_" + i + " (");
						for (VariableElement param : m.getParameters()) {
							String type = param.asType().toString();
							if (param.asType().getKind().isPrimitive()) {
								type = Constants.primitiveToObject.get(type);
							}
							buf.append(toString(param.getModifiers()) + " " + type + " " + param.getSimpleName() + ", ");
						}
						buf.append(c.getClazz().getSimpleName() + " " + Constants.TEST_OBJECT_PARAM_NAME + ", java.lang.reflect.Method "
								+ Constants.TEST_METHOD_PARAM_NAME);
						buf.append(") throws Exception {\n");
						// buf.append("return " +
						// Constants.TEST_METHOD_PARAM_NAME +
						// ".invoke("+Constants.TEST_OBJECT_PARAM_NAME+");\n");

						String formattedRule = formatRule(m.getSimpleName().toString(), rule);

						if (formattedRule == null || formattedRule.length() < 1)
							raiseError("Unparsable rule", m, i, "test");
						buf.append("return " + getCastString(m.getReturnType()) + " " + formatRule(m.getSimpleName().toString(), rule) + ";\n");
						buf.append("\n}");

						buf.append("public static ");
						buf.append(" boolean ");
						buf.append(m.getSimpleName() + "_Check" + i + " (" + m.getReturnType() + " orig, " + m.getReturnType() + " metamorphic)");
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
				if (!errorRaised)
					bw.append(buf);
			} catch (Exception ex) {
				raiseError("Unknown error " + ex.getMessage(), e);
			}
			try {
				bw.close();
			} catch (IOException ex) {
				raiseError("Unable to write file", e);
			}
		}
	}

	private String getCastString(TypeMirror type) {
		if (!type.getKind().isPrimitive())
			return "(" + type + ")";
		else {
			if (type.getKind().equals(TypeKind.INT))
				return "(Integer)";
		}
		return "(Object)";
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
				// check here that that the type is comparable

				if (rule.checkMethod().equals("!="))
					right = "! metamorphic.equals(" + right + ");";
				else if (rule.checkMethod().equals("=="))
					right = "metamorphic.equals(" + right + ");";
				else {
					if (this.processingEnv.getTypeUtils().isSubtype(returnType,
							this.processingEnv.getElementUtils().getTypeElement(Comparable.class.getName()).asType()))
						right = "metamorphic.compareTo(" + right + ") " + rule.checkMethod() + " 0;";
					else
						raiseError("Check method indicates a " + rule.checkMethod() + " check operation, but the return type (" + returnType
								+ ") is not Comparable", method, ruleIndex, "checkMethod");
				}
			}
		}
		// rule = rule.replaceAll(methodName+"(",
		// Constants.TEST_METHOD_PARAM_NAME+".invoke("+Constants.TEST_OBJECT_PARAM_NAME+(rule.contains(methodName+"()")
		// ? "" : ","));
		return right;
	}

}

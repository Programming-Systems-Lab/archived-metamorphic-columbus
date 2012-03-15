package edu.columbia.cs.psl.metamorphic.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@SupportedAnnotationTypes("edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
// @SupportedOptions( { AnnotationProcessor.OPT_VERBOSE,
// AnnotationProcessor.OPT_CLASSPATH})
public class AnnotationProcessor extends AbstractProcessor {
	// protected static final String OPT_VERBOSE = "verbose";
	// protected static final String OPT_CLASSPATH = "cp";
	// private final MetamorphicTestCompiler fCompiler =
	// MetamorphicTestCompiler.getInstance();
	private TypeElement getEnclosingClass(Element e) {
		if (e instanceof TypeElement)
			if (((TypeElement) e).getEnclosingElement() instanceof PackageElement)
				return (TypeElement) e;
		return getEnclosingClass(e.getEnclosingElement());

	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		HashMap<TypeElement, AnnotatedClass> classFiles = new HashMap<TypeElement, AnnotatedClass>();

		for (TypeElement typeElement: annotations) {
			 for ( Element element : roundEnv.getElementsAnnotatedWith(
			 typeElement ) ) {
			if (!classFiles.containsKey(getEnclosingClass(element))) {
				try {
					AnnotatedClass ac = new AnnotatedClass();
					ac.setClazz(getEnclosingClass(element));
					ac.setJfo(processingEnv.getFiler().createSourceFile(
							getEnclosingClass(element).getQualifiedName()
									+ "_tests"));
					ac.setMethods(new ArrayList<ExecutableElement>());
					classFiles.put(ac.getClazz(), ac);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		}

		if (annotations.size() > 0)
			processClasses(classFiles);
		return true;
	}
	private String toString(Set<Modifier> modifiers)
	{
		StringBuilder buf = new StringBuilder();
		for(Modifier mod : modifiers)
		{
			buf.append(mod.name() + " ");
		}
		return buf.substring(0, (buf.length() > 1? buf.length()-1 : 0)).toString();
	}
	private void generateCode(HashMap<TypeElement, AnnotatedClass> classFiles)
	{
		for (TypeElement e : classFiles.keySet()) {
			AnnotatedClass c = classFiles.get(e);
			try {
				BufferedWriter bw = new BufferedWriter(c.getJfo()
						.openWriter());
//				PrintStream bw = System.out;
				StringBuffer buf = new StringBuffer();
				buf.append("package ");
				buf.append(((PackageElement) c.getClazz()
						.getEnclosingElement()).getQualifiedName());
				buf.append(";\n");
				buf.append("public class "+c.getClazz().getSimpleName()+"_tests {\n");
				for(ExecutableElement m : c.getMethods())
				{
					int i = 0;
					for(String rule : m.getAnnotation(Metamorphic.class).rule())
					{
						buf.append("public static ");
						buf.append(" " + m.getReturnType()+ " ");
						buf.append(m.getSimpleName() + "_"+i+" (");
						for(VariableElement param : m.getParameters())
						{
							buf.append(toString(param.getModifiers())+ " " + param.asType().toString() +  " " + param.getSimpleName() + ", ");
						}
						buf.append(c.getClazz().getSimpleName() + " " + Constants.TEST_OBJECT_PARAM_NAME+", java.lang.reflect.Method "+Constants.TEST_METHOD_PARAM_NAME);
						buf.append(") throws Exception {\n");
//						buf.append("return " + Constants.TEST_METHOD_PARAM_NAME + ".invoke("+Constants.TEST_OBJECT_PARAM_NAME+");\n");
						buf.append("return " + getCastString(m.getReturnType()) + " "+ formatRule(m.getSimpleName().toString(),rule)+";\n");
						buf.append("\n}");
						
						buf.append("public");
						buf.append(" boolean ");
						buf.append(m.getSimpleName() + "_Check"+i+" ("+m.getReturnType()+" orig, "+m.getReturnType()+" metamorphic)");
						buf.append(" {\n");
						if(!m.getReturnType().getKind().isPrimitive())
							buf.append("if(orig == null && metamorphic != null) return false; if(orig == null && metamorphic == null) return true;");
						buf.append("return " +formatRuleCheck(m.getSimpleName().toString(),rule,m.getReturnType())+"\n");
						buf.append("\n}");
						
						i++;
					}
				}
//				bw.newLine();
				buf.append("\n}");
				bw.append(buf);
				bw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	private String getCastString(TypeMirror type) {
		if(!type.getKind().isPrimitive())
			return "("+type+")";
		else
		{
			if(type.getKind().equals(TypeKind.INT))
				return "(Integer)";
		}
		return "(Object)";
	}

	private String formatRule(String methodName, String rule) {
		rule = rule.replaceAll(methodName+"\\(", Constants.TEST_METHOD_PARAM_NAME+".invoke("+Constants.TEST_OBJECT_PARAM_NAME+(rule.contains(methodName+"()") ? "" : ","));
		rule = rule.substring(0,rule.indexOf("=="));
		Pattern p = Pattern.compile("\\\\([^(]+)\\(");
		Matcher m = p.matcher(rule);
		rule = m.replaceAll("new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.$1().apply((Object) ");
//		rule = rule.replaceAll("\\\\([^(]+)", "edu.columbia.cs.psl.metamorphic.inputProcessor.impl.");
		return rule;
	}

	private String formatRuleCheck(String methodName, String rule,TypeMirror returnType) {
		rule = rule.substring(rule.indexOf("==")+2);
		rule = rule.replace("\\result", "orig");
		if(returnType.getKind().isPrimitive())
			rule = "metamorphic == " + rule +";";
		else
			rule = "metamorphic.equals("+rule+");";
//		rule = rule.replaceAll(methodName+"(", Constants.TEST_METHOD_PARAM_NAME+".invoke("+Constants.TEST_OBJECT_PARAM_NAME+(rule.contains(methodName+"()") ? "" : ","));
		return rule;
	}
	
	private void processClasses(HashMap<TypeElement, AnnotatedClass> classFiles) {
		examineTypes(classFiles);
		generateCode(classFiles);
	}

	private void examineTypes(HashMap<TypeElement, AnnotatedClass> classFiles) {
		CompilerMethodVisitor v = new CompilerMethodVisitor();
		for(TypeElement e : classFiles.keySet())
		{
			e.accept(v, classFiles.get(e));
		}
		
	}

}

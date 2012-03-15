package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.columbia.cs.psl.metamorphic.annotation.Constants;
import edu.columbia.cs.psl.metamorphic.inputProcessor.impl.AddNumericConstant;

public class FoobarTest {
	private String foo(String bar)
	{
		return bar + "zzz";
	}
	private int test()
	{
		Integer i = new Integer(100);
		return (Integer) i;
	}
	private static String formatRule(String methodName, String rule) {
		rule = rule.replaceAll(methodName+"\\(", Constants.TEST_METHOD_PARAM_NAME+".invoke("+Constants.TEST_OBJECT_PARAM_NAME+(rule.contains(methodName+"()") ? "" : ","));
		rule = rule.substring(0,rule.indexOf("=="));
		Pattern p = Pattern.compile("\\\\([^(]+)\\(");
		Matcher m = p.matcher(rule);
		if(!m.matches())
		{
			System.out.println("No match");
			System.out.println(p);
		}

		rule = m.replaceAll("edu.columbia.cs.psl.metamorphic.inputProcessor.impl.$1.apply(");
//		rule = rule.replaceAll("\\\\([^(]+)", "edu.columbia.cs.psl.metamorphic.inputProcessor.impl.");
		return rule;
	}
	public static void main(String[] args) {
		System.out.println( formatRule("findClosestValue", "findClosestValue(\\MultiplyByNumericConstant(values, 10), target * 10) == \\result * 10"));
//		System.out.println();
		int x = new AddNumericConstant().apply(30,10);
		System.out.println(x);
	}
}

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/***************************

 * get rid of excess Java-only stuff
 * pointers


 **************************/

public class CProcessor
{
    private PrintWriter out = null;
    private Scanner in = null;

    // keeps track of the primitive types
    private static ArrayList<String> primitiveTypes = new ArrayList<String>();


    public static void main(String[] args)
    {
	if (args.length < 1)
	{
	    System.out.println("Please specify a file");
	    System.exit(0);
	}

	CProcessor p = new CProcessor();
	p.scan(args[0]);
    }


    public CProcessor()
    {
        primitiveTypes.add("byte");
	primitiveTypes.add("short");
	primitiveTypes.add("int");
	primitiveTypes.add("long");
	primitiveTypes.add("double");
	primitiveTypes.add("float");
	primitiveTypes.add("char");
    }

    public void scan(String origFile)
    {
	// keeps track of what level of curly braces we're at, 1 meaning outside any function defs
	int level = 1;

	// indicates whether we found a rule, and thus we need the corresponding method name
	boolean ruleFound = false;

	// holds all the rules we found
	ArrayList<String> rules = new ArrayList<String>();

	// whether or not we should be capturing the method name
	boolean captureName = false;

	// whether or not we're supposed to add our test method
	boolean addTestMethod = false;

	// the name of the method we're creating a test for
	Method method = null;

	// holds the name of the method as we read it from the file
	String methodLine = "";
        
	try
	{
	    String backup = origFile + ".bak";

	    // make a copy of the original
	    Runtime.getRuntime().exec("cp " + origFile + " " + backup);
            

	    System.out.println("scanning the backed-up file");

	    // to read the file
	    in = new Scanner(new File(backup));

	    // each line that we read
	    String line = "";

	    // to rewrite the original file
	    out = new PrintWriter(new File(origFile));


	    // first, add all the headers we'll need
	    // TODO: put this AFTER all the other headers... I think
	    out.println("#include <stdio.h>");
	    out.println("#include <signal.h>");
	    out.println("#define __USE_GNU");
	    out.println("#include <sched.h>");


	    while (in.hasNext())
	    {
		// read the next line of the file in its entirety
		line = in.nextLine();
		System.out.println("READ: " + line);

		if (line.trim().startsWith("@meta"))
		{
		    
		}
		else if (line.trim().endsWith("*/"))
                {
		    out.println(line);
		}
		else if (captureName)
                {
		    methodLine += " " + line.trim();

		    if (line.contains(")"))
		    {
			method = getMethod(methodLine.trim());
			System.out.println("METHODNAME IS " + method.name);

			// because we're going to wrap this function, we need to change its name
			String newName = methodLine.replace(method.name, "__" + method.name);
			out.println(newName);
		    }
		}
		else
                {
		    out.println(line);
		}
		
		// trim it
		line = line.trim();


		// see what level of curly braces we're at
		if (line.contains("{")) level++;
		if (line.contains("}")) level--;

		// special case
		//if (captureName && line.contains("{")) level = 1;

		System.out.println("LEVEL IS " + level);

		// we only care about such things at the method level
		if (level == 1 || (captureName && line.contains("{")) || line.startsWith("@"))
		{
		    if (line.startsWith("@meta") )
		    {
			// we found a rule!!!
			line = line.split("@meta")[1].trim().replace(";", "");
			rules.add(line);
			System.out.println("Rule " + line);
			ruleFound = true;
			out.println("\t@meta " + line + ";");
		    }
		    else if (ruleFound && line.endsWith("*/"))
		    {
			System.out.println("Need to capture name");
			// this means we are at the end of the comment and
			// we need to get ready for the method name
			captureName = true;
			ruleFound = false;
		    }
		    else if (captureName)
		    {
			// this is where we are reading the name of the method
			System.out.println("The method so far is " + methodLine);
			// we know it's the end of the function header if it contains a right paren
			if (line.contains(")"))
			{
			    // add this last bit to the methodLine
			    method = getMethod(methodLine);
			    methodLine = "";
			    captureName = false;
			    addTestMethod = true;
			}
		    }
		    else if (addTestMethod)
		    {
			// now we're creating the test method
			System.out.println("Adding test method");
			addTestMethod = false;
                        if (rules.isEmpty()==false)
			{
			    // create the test method first because it's called by the wrapper
                            out.println(createTestMethod(rules, method));
			    out.println(createWrapperMethod(method));
			}
			rules.clear();
		    }			
		}

	    }

	    System.out.println("Done reading " + origFile);

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    try { out.flush(); } catch (Exception e) { }
	    try { out.close(); } catch (Exception e) { }
	}

	

    }

    /**
     * Reads in a line and pulls out the name of the method, along with its parameters
     * If includeTypes is true, then it also includes the types of the parameters, like in a declaration.
     * If includeTypes is false, then it removes the parameter types, like in an invocation.
     */
    public static Method getMethod(String line)
    {
	System.out.println("CONVERTING " + line + " to Method");

	Method method = new Method();

	if (line.contains("static "))
	{
	    method.isStatic = true;
	    line = line.replace("static ", "");
	}

	if (line.contains("extern "))
	{
	    method.isExtern = true;
	    line = line.replace("extern ", "");
	}

	if (line.contains("inline "))
	{
	    method.isExtern = true;
	    line = line.replace("inline ", "");
	}

	// in case the curly brace is on this line
	line = line.replace("{", "");
	line = line.trim();
	
	// scan the line
	Scanner reader = new Scanner(line);

	// get everything else
	line = reader.nextLine().trim();

	// this is the name of the method and, if it exists, the return type
	String firstPart = line.split("\\(")[0].trim();

	// if there's a whitespace in firstPart, then it contains the return type
	if (firstPart.contains(" "))
	{
	    int lastIndex = firstPart.lastIndexOf(' ');
	    // the return type includes everything up to the last whitespace
	    method.returnType = firstPart.substring(0, lastIndex).trim();
	    // the name is everything from the last whitespace to the end
	    method.name = firstPart.substring(lastIndex).trim();
	}
	else
	{
	    method.returnType = ""; // IS THIS OKAY??
	    method.name = firstPart;
	}

	// this is the list of parameters, without the parentheses
	method.params = line.split("\\(")[1].replace(")", "");
	System.out.println("PARAMS: " + method.params);

	return method;
	
    }


    private String createWrapperMethod(Method method) throws Exception
    {
	StringBuffer retVal = new StringBuffer();

	// the method header
	retVal.append(method.permission + " ");
	if (method.isExtern) retVal.append("extern ");
	if (method.isStatic) retVal.append("static ");
	if (method.isInline) retVal.append("inline ");
	retVal.append(method.returnType + " " + method.name + " (" + method.params + ") " + method.throwsPart + " {\n");
	
	// to hold the parameters
	String paramNames = null;
	if (method.params.equals("void")) paramNames = "";
	else paramNames = getParamNames(method.params);

	// remove pointer dereferences
	paramNames = paramNames.replace("*", "");

	// determine if the return type is void
	boolean isVoid = method.returnType.equals("void") || method.returnType.equals("");

	// if not a void function, call the wrapped method to get the return value
	if (!isVoid) retVal.append(" " + method.returnType + " result = __" + method.name + "(" + paramNames + ");\n");
	// otherwise, just call the function
	else retVal.append(" __" + method.name + "(" + paramNames + ");\n");

	// see if a test is necessary
	retVal.append(" if (should_run_test(\"" + method.name + "\")) {\n");
	//retVal.append(" if (1) {\n");

	// fork
	retVal.append("   int pid = fork();\n");
	retVal.append("   signal(SIGCHLD, SIG_IGN);\n");
	
	// if it's the child, run the test
	retVal.append("   if (pid == 0) {\n");

	// set the processor affinity
	retVal.append("      cpu_set_t mask;\n");
	retVal.append("      CPU_ZERO(&mask);\n");
	retVal.append("      CPU_SET(1, &mask);\n");
	retVal.append("      sched_setaffinity(0, sizeof(mask), &mask);\n");


	// see if the test passes
	String testMethodName = "__test_" + method.name;
	
	// if it's a void function, we don't have a result to send
	if (!isVoid) retVal.append("      if (" + testMethodName + "(" + paramNames + ", result) == 0) {\n");
	else retVal.append("      if (" + testMethodName + "(" + paramNames + ") == 0) {\n");

	// handle the failed test
	retVal.append("           printf(\"TEST FAILED\\n\");\n");
	retVal.append("      }\n");

	// handle the successful test
	retVal.append("      else {\n");
	retVal.append("           printf(\"test passed\\n\");\n");
	retVal.append("      }\n");
	

	// kill the child process
	retVal.append("      exit(0);\n");
	retVal.append("   }\n");


	retVal.append(" }\n");
	
	// return the result if it's not void
	if (!isVoid) retVal.append("return result;\n");
	else retVal.append("return;\n");

	retVal.append("}\n");

	return retVal.toString();
		      
    }

    private String getParamNames(String params)
    {
	StringBuffer names = new StringBuffer();
	StringTokenizer tok = new StringTokenizer(params, ",");

	int tokens = tok.countTokens();
	int counter = 0;
	
	while (tok.hasMoreTokens())
	{
	    String param = tok.nextToken().trim();
	    //System.out.println("PARAM[" + counter + "] " + param);
	    // need to split on the last whitespace since the type may also contain spaces
	    int lastIndex = param.lastIndexOf(' ');
	    names.append(param.substring(lastIndex).trim());
	    if (counter < tokens - 1) names.append(", ");
	    counter++;
	}

	return names.toString();

    }


    private String createTestMethod(ArrayList<String> rules, Method m) throws Exception
    {
	// get the method name
	String methodName = m.name;

	// this is the String representation of the method
	StringBuffer method = new StringBuffer();

	String testMethodName = "__test_" + methodName;
	if (m.returnType.equals("void") || m.returnType.equals(""))
	    method.append(" int " + testMethodName + "(" + m.params + ") {\n");
	else
	    method.append(" int " + testMethodName + "(" + m.params + ", " + m.returnType + " result) {\n");
        
	//for (String rule : rules) System.out.println(rule);
	for (String rule : rules)
	{
            
            // parse the rule to check for any keyword usage
            rule = parseRule(rule, methodName);

	    // change the method name in the rule
	    rule = rule.replace(" " + methodName, " __" + methodName);
            
	    if (rule.contains("if ") || rule.contains("}") || rule.contains("{")) 
                method.append(rule + "\n");
	    else method.append("        if ((" + rule + ") == 0) return 0;\n");
	}

	// if we made it here, everything is okay
	method.append("        return 1;\n");

	// end of the method
	method.append(" }\n");

	return method.toString();
    }
    
    private static String parseRule(String rule, String methodName)
    {
        // detect keyword "/result" and replace it with the return value of method, prohibited to use on methods with void return
        //rule = rule.replace("\\result", funcall(methodName));
        rule = rule.replace("\\result", "result");
        
        //System.out.println("replacing "+rule);
        
	/*
        // grammar: shuffle(param), param can be any List, array of primitive types of objects
        rule = rule.replace("shuffle", "RuleProcessor.shuffle");
        

        // grammar: reverse(param) where param is any List or array
        rule = rule.replace("reverse", "RuleProcessor.reverse");
        

        // grammar: negate(param) where param is an array of numeric primitive types
        rule = rule.replace("negate", "RuleProcessor.negate");
        

        // grammar: valueIn(param1, param2) where param1 is an Object or value in primitive types, param2 is an array of objects or variables of the same type of param1
        rule = rule.replace("valueIn", "RuleProcessor.valueIn");
        
        
        // grammar: inRange(param1, param2, param3), return true if param2 <= param1 <= param3, 3 parameters must be the same type => either Comparable objects or numeric primitive types
        rule = rule.replace("inRange", "RuleProcessor.inRange");

        
        // grammar: approximatelyEqualTo(value1, value2, offset), return true if the difference between value1 and value2 is less than the offset, values compared musst be numeric
        rule = rule.replace("approximatelyEqualTo", "RuleProcessor.approximatelyEqualTo");
	*/
        
        return rule;
    }
    

}
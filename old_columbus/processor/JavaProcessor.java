import java.util.*;
import java.io.*;
import java.lang.reflect.*;



public class JavaProcessor
{
    private PrintWriter out = null;
    private Scanner in = null;

    public static boolean VERBOSE = false;

    // keeps track of the primitive types
    private static ArrayList<String> primitiveTypes = new ArrayList<String>();


    public static void main(String[] args)
    {
	System.out.println("");
	System.out.println("Columbus pre-processor for Java");
	System.out.println("(c)2009 Columbia University. All rights reserved.");
	System.out.println("=================================================\n");

	if (args.length < 1)
	{
	    showHelp();
	    System.exit(0);
	}

	JavaProcessor p = new JavaProcessor();

	if (args.length > 1)
	    if (args[1].equals("-verbose") || args[1].equals("-v"))
		JavaProcessor.VERBOSE = true;

	p.scan(args[0]);
    }

    public static void showHelp()
    {
	System.out.println("Usage: java JavaProcessor <filename> [-verbose]\n");

	System.out.println("Press Enter to see the help screen.");
	Scanner in = new Scanner(System.in);
	in.nextLine();

	System.out.println("This utility converts annotations specifying metamorphic");
	System.out.println("properties to tests that are executed at runtime.\n");
	System.out.println("Be sure the properties are specified in a comment block");
	System.out.println("that starts like this: /*@");
	System.out.println("Each property is then specified on a line starting: @meta\n");
	System.out.println("Press Enter to continue...");
	in.nextLine();

	System.out.println("Here are the special keywords you can use in the specifications:\n");
	System.out.println("\\result: refers to the return value of the original function call");
	System.out.println("\\add(A, v): adds a constant value v to all elements in the array A");
	System.out.println("\\multiply(A, v): multiplies all elements in A by v");
	System.out.println("\\negate(A): multiplies all elements in A by -1");
	System.out.println("\\shuffle(A): randomly shuffles the elements in array A");
	System.out.println("\\reverse(A): reverses the order of the elements in array A\n");
	System.out.println("Press Enter to continue...");
	in.nextLine();

	System.out.println("Here's an example for a function that calculates the average of");
	System.out.println("an array of integers:\n");
	System.out.println("/*@");
	System.out.println("  @meta average(\\shuffle(A)) == \\result");
	System.out.println("  @meta average(\\negate(A)) == \\result * -1");
	System.out.println("  @meta average(\\add(A, 3)) == \\result + 3");
	System.out.println("*/");
	System.out.println("public double average(int[] A) { ... }\n");
	System.out.println("Press Enter to continue...");
	in.nextLine();

	System.out.println("If you encounter any errors, please contact cmurphy@cs.columbia.edu");
    }


    public JavaProcessor()
    {
        primitiveTypes.add("byte");
	primitiveTypes.add("short");
	primitiveTypes.add("int");
	primitiveTypes.add("long");
	primitiveTypes.add("double");
	primitiveTypes.add("float");
	primitiveTypes.add("boolean");
	primitiveTypes.add("char");
	primitiveTypes.add("java.lang.String"); // not technically a primitive, but still works the same!
    }

    public void scan(String origFile)
    {
	// keeps track of what level of curly braces we're at, 0 meaning outside the class
	int level = 0;

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
        
	try
	{
	    String backup = origFile + ".bak";

	    // make a copy of the original
	    System.out.println("Copying " + origFile + " to " + backup);
	    Runtime.getRuntime().exec("cp " + origFile + " " + backup);
            
	    // to read the file
	    in = new Scanner(new File(backup));

	    // each line that we read
	    String line = "";

	    // to rewrite the original file
	    out = new PrintWriter(new File(origFile));


	    // the name of the original class
	    String className = "";

	    // to hold the level at which a method is encountered
	    int methodLevel = 0;
		

	    while (in.hasNext())
	    {
		// read the next line of the file in its entirety
		line = in.nextLine();
		if (VERBOSE) System.out.println("READ: " + line);

		// if it's at the top level, just print it out
		if (level == 0 && line.contains("{") == false)
		{
		    //out.println(line);
		}

		if (level == 0 && line.contains("class"))
		{
		    // this part reads the line containing the class declaration and breaks it apart

		    String permission = line.split("class")[0].trim();
		    //System.out.println("Permission " + permission);

		    String classPart = line.split("class")[1].trim();
		    //System.out.println("ClassPart " + classPart);

		    className = classPart.split(" ")[0].trim();
		    //System.out.println("ClassName " + className);
		    className = className.replace("{", "");
		    
		    out.println(line);
		}
		else
		{
		    //System.out.println(line);

		    // TODO: not sure whether the first parts are needed

                    if (line.trim().startsWith("@meta"))
                    {

                    }
                    else if (line.trim().endsWith("*/"))
                    {
                        out.println(line);
                    }
                    else if (captureName)
                    {
                        method = getMethod(line);

			// because we're going to wrap this function, we need to change its name
			String newName = line.replace(method.name, "__" + method.name);
                        out.println(newName);
                    }
                    else
                    {
                        out.println(line);
                    }
		}

		
	     		
		// trim it
		line = line.trim();


		// see what level of curly braces we're at
		if (line.contains("{")) level++;
		if (line.contains("}")) level--;

		// special case
		//if (captureName && line.contains("{")) level = 1;

		if (VERBOSE) System.out.println("LEVEL IS " + level);


		// we only care about such things at the method level
		if (level >= 1 || (captureName && line.contains("{")) || line.startsWith("@"))
		{
		    if (line.startsWith("@meta") )
		    {
			// we found a rule!!!
			line = line.split("@meta")[1].trim().replace(";", "");
			rules.add(line);
			System.out.println("Found rule " + line);
			ruleFound = true;
			out.println("\t@meta " + line + ";");
		    }
		    else if (ruleFound && line.endsWith("*/"))
		    {
			if (VERBOSE) System.out.println("Need to capture name");
			// this means we are at the end of the comment and
			// we need to get ready for the method name
			captureName = true;
			ruleFound = false;
			methodLevel = level;
		    }
		    else if (captureName)
		    {
			// this is where we are reading the name of the method
			if (VERBOSE) System.out.println("The method is " + line);
			method = getMethod(line);
			captureName = false;
			addTestMethod = true;
		    }
		    else if (addTestMethod && level == methodLevel)
		    {
			// now we're creating the test method
			if (VERBOSE) System.out.println("Adding test method");
			addTestMethod = false;
                        if (rules.isEmpty()==false)
			{
			    out.println(createWrapperMethod(method, className));
                            out.println(createTestMethod(rules, method, className));
			}
			rules.clear();
		    }			
		}

	    }

	    System.out.println("Done reading " + origFile);

	}
	catch (Exception e)
	{
	    System.out.println("*****************************************");
	    System.out.println("An error occurred in processing the file!");
	    System.out.println("Please send the following to cmurphy@cs.columbia.edu:");
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
	if (VERBOSE) System.out.println("CONVERTING " + line + " to Method");

	Method method = new Method();

	// get rid of any words that could be there
	if (line.contains("public "))
	{
	    method.permission = "public";
	    line = line.replace("public ", "");
	}
	else if (line.contains("private "))
	{
	    method.permission = "private";
	    line = line.replace("private ", "");
	}
	else if (line.contains("protected "))
	{
	    method.permission = "protected";
	    line = line.replace("protected ", "");
	}

	if (line.contains("static "))
	{
	    method.isStatic = true;
	    line = line.replace("static ", "");
	}

	if (line.contains("synchronized "))
	{
	    method.isSynchronized = true;
	    line = line.replace("synchronized ", "");
	}

	if (line.contains("final "))
	{
	    method.isFinal = true;
	    line = line.replace("final ", "");
	}

	if (line.contains("throws "))
	{
	    String throwsPart = line.split("throws ")[1].replace("{", "");
	    method.throwsPart = "throws " + throwsPart;
	    line = line.replace(method.throwsPart, "");
	}


	line = line.replace("{", "");
	line = line.trim();
	
	// scan the line
	Scanner reader = new Scanner(line);

	// the first token will be the return type
	method.returnType = reader.next();

	// everything else is the method name, including the parameters and their types
	String methodName = reader.nextLine().trim();

	// this is the name of the method
	method.name = methodName.split("\\(")[0];
	//System.out.println("NAME:" + name);

	// this is the list of parameters, without the parentheses
	method.params = methodName.split("\\(")[1].replace(")", "");
	//System.out.println("PARAMLIST: " + paramList);

	/*
	// if there are no parameters, we're done
	if (paramList.equals("")) return name + "()";

	// this is the array of each individual parameter, with its type
	String[] params = paramList.split(",");

	// start the parens for the return value
	name += "(";

	// now add each param to the list, except the last one
	for (int i = 0; i < params.length - 1; i++)
	{
	    String param = params[i];
	    //System.out.println("PARAM: " + param.trim());
	    name += param.split(" ")[1] + ", ";
	}

	String param = params[params.length - 1];
	//System.out.println("PARAM: " + param.trim());
	name += param.split(" ")[1] + ")";

	return name;
	*/

	//System.out.println(method);

	return method;
	
    }


    private String createWrapperMethod(Method method, String className) throws Exception
    {
	StringBuffer retVal = new StringBuffer();

	// the method header
	retVal.append(method.permission + " ");
	if (method.isSynchronized) retVal.append("synchronized ");
	if (method.isStatic) retVal.append("static ");
	if (method.isFinal) retVal.append("final ");
	retVal.append(method.returnType + " " + method.name + " (" + method.params + ") " + method.throwsPart + " {\n");
	
	// to hold the parameters
	String paramNames = getParamNames(method.params);

	// determine if the return type is void
	boolean isVoid = method.returnType.equals("void");

	// if not a void function, call the wrapped method to get the return value
	if (!isVoid) retVal.append(" " + method.returnType + " result = __" + method.name + "(" + paramNames + ");\n");
	// otherwise, just call the function
	else retVal.append(" __" + method.name + "(" + paramNames + ");\n");

	// see if a test is necessary
	retVal.append(" if (columbus.Columbus.shouldRunTest(\"" + className + "." + method.name + "\")) {\n");

	// fork
	retVal.append("   int pid = columbus.Forker.fork();\n");
	
	// if it's the child, run the test
	retVal.append("   if (pid == 0) {\n");

	// set the processor affinity
	retVal.append("      columbus.Affinity.setAffinity(1);\n");

	// see if the test passes
	String testMethodName = "__test" + method.name.substring(0,1).toUpperCase() + method.name.substring(1, method.name.length());
	
	// if it's a void function, we don't have a result to send
	if (!isVoid) retVal.append("      if (" + testMethodName + "(" + paramNames + ", result) == false) {\n");
	else retVal.append("      if (" + testMethodName + "(" + paramNames + ") == false) {\n");

	// handle the failed test
	retVal.append("           System.out.println(\"TEST FAILED: " + method.name +" \");\n");
	retVal.append("      }\n");

	// handle the successful test
	retVal.append("      else {\n");
	retVal.append("           //System.out.println(\"test passed\");\n");
	retVal.append("      }\n");
	

	// kill the child process
	retVal.append("      columbus.Forker.exit();\n");
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
	    if (VERBOSE) System.out.println("PARAM[" + counter + "] " + param);
	    names.append(param.split(" ")[1].trim());
	    if (counter < tokens - 1) names.append(", ");
	    counter++;
	}

	return names.toString();

    }


    private String createTestMethod(ArrayList<String> rules, Method m, String className) throws Exception
    {
	// get the method name
	String methodName = m.name;

	// this is the String representation of the method
	StringBuffer method = new StringBuffer();

	String testMethodName = "__test" + methodName.substring(0,1).toUpperCase() + methodName.substring(1, methodName.length());
	if (m.returnType.equals("void"))
	    method.append(" public synchronized boolean " + testMethodName + "(" + m.params + ") {\n");
	else
	    method.append(" public synchronized boolean " + testMethodName + "(" + m.params + ", " + m.returnType + " result) {\n");
        
	// keeps track of what got backed up
	ArrayList<String> backedUp = new ArrayList<String>();

	// everything is in a try block
	method.append("    try {\n");

	//for (String rule : rules) System.out.println(rule);
	for (String rule : rules)
	{
            
            // parse the rule to check for any keyword usage
            rule = parseRule(rule, methodName);

	    // replace double-equals with approximatelyEqualTo function 
	    if (rule.contains("=="))
	    {
		String[] parts = rule.split("==");
		
		if (parts.length == 2)
		{
		    String left = parts[0].trim();
		    String right = parts[1].trim();
		    
		    rule = "columbus.RuleProcessor.approximatelyEqualTo(" + left + ", " + right + ")";
		    System.out.println("Replaced == to " + rule);
		}
		else
		    System.out.println("WARNING: Could not replace double-equals in " + rule);
	    }

	    // change the method name in the rule
	    rule = rule.replace(methodName, "__" + methodName);
	    if (VERBOSE) System.out.println("Rewrote rule: " + rule);
            
	    if (rule.contains(" if") || rule.contains("}") || rule.contains("{")) 
                method.append(rule + "\n");
	    else method.append("        if (!(" + rule + ")) return false;\n");
	}

	// if we made it here, everything is okay
	method.append("        return true;\n");

	// end of try block
	method.append("    }\n");

	// catch block
	method.append("    catch (Exception e) { e.printStackTrace(); }\n");

	// now the finally block
	method.append("    finally {\n");

	// if there are backed-up variables, restore them
	for (String variable : backedUp)
	{
	    method.append("        " + variable + " = __" + variable + ";\n");
	}
	
	// end of the finally block
	method.append("    }\n");

	// return false if we get here
	method.append("    return false;\n");

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
        

        // grammar: shuffle(param), param can be any List, array of primitive types of objects
        rule = rule.replace("\\shuffle", "columbus.RuleProcessor.shuffle");
        

        // grammar: reverse(param) where param is any List or array
        rule = rule.replace("\\reverse", "columbus.RuleProcessor.reverse");
        

        // grammar: negate(param) where param is an array of numeric primitive types
        rule = rule.replace("\\negate", "columbus.RuleProcessor.negate");

        // grammar: multiply(param, val) where param is an array of numeric primitive types and val is of the same type
        rule = rule.replace("\\multiply", "columbus.RuleProcessor.multiply");

        // grammar: add(param, val) where param is an array of numeric primitive types and val is of the same type
        rule = rule.replace("\\add", "columbus.RuleProcessor.add");
        

        // grammar: valueIn(param1, param2) where param1 is an Object or value in primitive types, param2 is an array of objects or variables of the same type of param1
        rule = rule.replace("\\valueIn", "columbus.RuleProcessor.valueIn");
        
        
        // grammar: inRange(param1, param2, param3), return true if param2 <= param1 <= param3, 3 parameters must be the same type => either Comparable objects or numeric primitive types
        rule = rule.replace("\\inRange", "columbus.RuleProcessor.inRange");

        
        // grammar: approximatelyEqualTo(value1, value2, offset), return true if the difference between value1 and value2 is less than the offset, values compared musst be numeric
        rule = rule.replace("\\approximatelyEqualTo", "columbus.RuleProcessor.approximatelyEqualTo");
        
        return rule;
    }
    


}
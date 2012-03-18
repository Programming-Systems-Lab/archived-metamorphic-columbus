package edu.columbia.cs.psl.metamorphic.compiler;

import java.util.HashMap;

public class Constants {
	public static final String TEST_OBJECT_PARAM_NAME = "___object";
	public static final String TEST_METHOD_PARAM_NAME = "___method";
	public static HashMap<String, String> primitiveToObject = new HashMap<String, String>();
	public static final String TEST_OUTPUT_JAR = "__columbus.jar";
	static
	{
		primitiveToObject.put("int", "Integer");
		primitiveToObject.put("double", "Double");
		primitiveToObject.put("short", "Short");
		primitiveToObject.put("long", "Long");
		primitiveToObject.put("float", "Float");
		primitiveToObject.put("byte", "Byte");
		primitiveToObject.put("char", "Character");
		primitiveToObject.put("boolean", "Boolean");
	}
}

package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;

import edu.columbia.cs.psl.mountaindew.runtime.annotation.Metamorphic;


public class Interceptor{
	public Interceptor(Object intercepted)
	{
		System.out.println("We are intercepting object " + intercepted);
	}
	public void onEnter(Method method, Object[] params)
	{
		System.out.println("We have entered the method" + method);
		for(Object o : params)
		{
			System.out.println("Param: <"+o+">");
		}
	}
	
	public void onExit(Object val, int op)
	{
		System.out.println("On exit: <" + val+"> " + op);
	}
} 


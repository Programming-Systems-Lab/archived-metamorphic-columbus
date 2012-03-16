package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

/**
 * Each intercepted object will have its _own_ Interceptor instance.
 * That instance will stick around for the lifetime of the intercepted object.
 * 
 * NB if you want to keep a list of these Interceptors somewhere statically,
 * you probably want to use a WeakHashMap so as to not create memory leaks
 * 
 * @author jon
 *
 */
public class Interceptor extends AbstractInterceptor {

	public Interceptor(Object intercepted) {
		super(intercepted);
	}

	public void onEnter(Method method, Object[] params)
	{
		System.out.println("We have entered the method <" + method +"> on the object <" + getInterceptedObject()+">" );
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


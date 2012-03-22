package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;



import edu.columbia.cs.psl.invivo.runtime.AbstractInterceptor;
import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;
import edu.columbia.cs.psl.metamorphic.struct.MetamorphicMethodInvocation;

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
	private HashMap<Integer, MetamorphicMethodInvocation> invocations = new HashMap<Integer, MetamorphicMethodInvocation>();
	private Integer invocationId = 0;
	private Class<?> testerClass;
	private Logger logger = Logger.getLogger(Interceptor.class);
	public Interceptor(Object intercepted) {
		super(intercepted);
		try {
			testerClass = Class.forName(intercepted.getClass().getCanonicalName()+"_tests");
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int onEnter(Object callee, Method method, Object[] params)
	{
		if(isChild(callee))
			return -1;
		
		int retId = 0;
		//Get our invocation id
		synchronized(invocationId)
		{
			invocationId++;
			retId = invocationId;	
		}
		//Create a new invocation object to store
		final MetamorphicMethodInvocation inv = new MetamorphicMethodInvocation();
		inv.params = params;
		inv.method = method;
		inv.callee = callee;
		inv.orig_params = deepClone(params); //Used for the check method, in case you care to refer to them in the rule

		//Find the rules
		Rule[] rules = method.getAnnotation(Metamorphic.class).rules();
		
		//Create the arrays needed for invoking methods
		
		Class<?>[] checkTypes = new Class[params.length + 2]; //First two params will be the two return values
		checkTypes[0] = method.getReturnType();
		checkTypes[1] = method.getReturnType();
		
		Class<?>[] childTestParamTypes = new Class[params.length + 2];	//Last two params will be the callee and the method
		childTestParamTypes[params.length] = callee.getClass();
		childTestParamTypes[params.length+1] = Method.class;
		
		Object[] childParams = new Object[params.length + 2];
		for(int i = 0; i< params.length; i++)
		{
			childTestParamTypes[i] = params[i].getClass();
			childParams[i] = params[i];
			checkTypes[i+2] = params[i].getClass();
		}
		
		
		invocations.put(retId, inv);
		
		inv.children = new MetamorphicMethodInvocation[rules.length];
		for(int i = 0; i < rules.length;i++)
		{
			inv.children[i] = new MetamorphicMethodInvocation();
			inv.children[i].parent = inv;
			inv.children[i].callee = deepClone(inv.callee);
			((MetamorphicMethodInvocation) inv.children[i]).rule = rules[i];
			try {
				inv.children[i].method = getMethod(inv.method.getName()+"_"+i, childTestParamTypes,testerClass);
				inv.children[i].checkMethod = getMethod(inv.method.getName()+"_Check"+i, checkTypes,testerClass);
			} catch (SecurityException e1) {
				logger.error("Error looking up method/check method for " + inv.method.getName()+"_"+i, e1);
			} catch (NoSuchMethodException e1) {
				logger.error("Error looking up method/check method for " + inv.method.getName()+"_"+i, e1);
			}
			inv.children[i].params = deepClone(childParams);
			inv.children[i].params[params.length] = inv.children[i].callee;
			inv.children[i].params[params.length +1 ] = method;
			
			inv.children[i].thread= createChildThread(inv.children[i]);
			inv.children[i].thread.start();
		}
		return retId;
	}
	
	public void onExit(Object val, int op, int id)
	{
		if(id < 0)
			return;
		try
		{
		MetamorphicMethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		Object[] checkParams = new Object[inv.params.length + 2];
		for(int i =0;i<inv.params.length;i++)
			checkParams[i+2] = inv.orig_params[i];
		for(MethodInvocation i : inv.children)
		{
			i.thread.join();
			logger.info("\tChild"+getChildId(i.callee) +" finished");
			
			
			checkParams[0] = val;
			checkParams[1] = i.returnValue;
			if(((Boolean)i.checkMethod.invoke(null, checkParams)) == false)
			{
				throw new IllegalStateException("Metamorphic property has been violated on " + inv.method +". Rule: [" + ((MetamorphicMethodInvocation) i).rule +"]. Outputs were [" + val+"], ["+i.returnValue+"]");
			}
		}
		logger.info("Invocation result: " + inv);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
} 


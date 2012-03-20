package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.rits.cloning.Cloner;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;
import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;

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
	private HashMap<Integer, MethodInvocation> invocations = new HashMap<Integer, MethodInvocation>();
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
	private Cloner cloner = new Cloner();
	public int onEnter(Object callee, Method method, Object[] params)
	{
		if(isChild(callee))
			return -1;
		
		int retId = 0;
		synchronized(invocationId)
		{
			invocationId++;
			retId = invocationId;	
		}
		final MethodInvocation inv = new MethodInvocation();
		inv.params = params;
		inv.method = method;
		inv.callee = callee;
		inv.params_cloned = cloner.deepClone(params);
		
		/**
		 * Make some changes here to instead apply the requested properties
		 */
		Rule[] rules = method.getAnnotation(Metamorphic.class).rules();
		Class<?>[] childTestParamTypes = new Class[params.length + 2];
		Object[] childParams = new Object[params.length + 2];
		
		Class<?>[] checkTypes = new Class[params.length + 2];
		checkTypes[0] = method.getReturnType();
		checkTypes[1] = method.getReturnType();
		
		for(int i = 0; i< params.length; i++)
		{
			childTestParamTypes[i] = params[i].getClass();
			childParams[i] = params[i];
			checkTypes[i+2] = params[i].getClass();
		}
		childParams[params.length] = callee;
		childParams[params.length +1 ] = method;
		
		childTestParamTypes[params.length] = callee.getClass();
		childTestParamTypes[params.length+1] = Method.class;
		
		
		
		
		invocations.put(retId, inv);
		inv.children = new MethodInvocation[rules.length];
		for(int i = 0; i < rules.length;i++)
		{
			final int k = i;
			inv.children[i] = new MethodInvocation();
			inv.children[i].rule = rules[i];
			try {
				inv.children[i].method = getMethod(inv.method.getName()+"_"+i, childTestParamTypes,testerClass);
				inv.children[i].checkMethod = getMethod(inv.method.getName()+"_Check"+i, checkTypes,testerClass);
//				System.out.println(inv.children[i].checkMethod);
				inv.children[i].params = childParams;
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			inv.children[i].thread= new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Object clone = cloner.deepClone(inv.callee);
						inv.children[k].params[inv.params.length] = clone;
						setAsChild(clone);
						logger.debug("Calling " + inv.children[k].method);
						for(int j = 0; j < inv.children[k].params.length;j ++)
						{
							logger.debug("Param: " + inv.children[k].params[j]);
						}
						inv.children[k].returnValue = inv.children[k].method.invoke(null, inv.children[k].params);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			});
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
		MethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		Object[] checkParams = new Object[inv.params.length + 2];
		for(int i =0;i<inv.params.length;i++)
			checkParams[i+2] = inv.params_cloned[i];
		for(MethodInvocation i : inv.children)
		{
			i.thread.join();
			checkParams[0] = val;
			checkParams[1] = i.returnValue;
			if(((Boolean)i.checkMethod.invoke(null, checkParams)) == false)
			{
				throw new IllegalStateException("Metamorphic property has been violated on " + inv.method +". Rule: [" + i.rule +"]. Outputs were [" + val+"], ["+i.returnValue+"]");
			}
		}
		System.out.println("Invocation result: " + inv);
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
} 


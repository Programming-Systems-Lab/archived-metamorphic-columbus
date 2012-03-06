package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.processor.impl.Shuffle;
import edu.columbia.cs.psl.metamorphic.runtime.visitor.InterceptingClassVisitor;
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
	
	public Interceptor(Object intercepted) {
		super(intercepted);
	}
	
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
		
		inv.childParams = inv.params;
		
		/**
		 * Make some changes here to instead apply the requested properties
		 */
		Shuffle s = new Shuffle();
		for(Object v : inv.childParams)
		{
			try
			{
				v = s.apply(v);
			}
			catch(IllegalArgumentException ex)
			{
				//DO nothing, this property just wasn't applicable
			}
		}
		invocations.put(retId, inv);
		
		inv.childThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Object clone = inv.callee.getClass().getMethod(InterceptingClassVisitor.CLONE_OVERRIDE_METHOD).invoke(inv.callee);
					setAsChild(clone);
					Object[] params = new Object[inv.childParams.length];
					for(int i = 0;i<inv.childParams.length;i++)
					{
						params[i]=inv.childParams[i];
					}
					inv.childReturnValue = inv.method.invoke(clone, params);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
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
		inv.childThread.start();
		return retId;
	}
	
	public void onExit(Object val, int op, int id)
	{
		if(id < 0)
			return;
		MethodInvocation inv = invocations.remove(id);
		inv.returnValue = val;
		try {
			inv.childThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Invocation result: " + inv);
	}
	
} 


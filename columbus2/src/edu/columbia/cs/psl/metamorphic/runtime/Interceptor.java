package edu.columbia.cs.psl.metamorphic.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.ipc.IPCManager;
import edu.columbia.cs.psl.metamorphic.processor.impl.Shuffle;
import edu.columbia.cs.psl.metamorphic.runtime.visitor.InterceptingClassVisitor;
import edu.columbia.cs.psl.metamorphic.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.struct.Variable;
import edu.columbia.cs.psl.metamorphic.util.Forker;

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
		IPCManager mgr = IPCManager.getInstance();
		if(mgr.isChild)
			return -1;
		
		int retId = 0;
		synchronized(invocationId)
		{
			invocationId++;
			retId = invocationId;	
		}
		final MethodInvocation inv = new MethodInvocation();
		inv.params = new Variable[params.length];
		for(int i=0;i<params.length;i++)
		{
			Variable v = new Variable();
			v.position = i;
			v.value = params[i];
			inv.params[i]=v;
		}
		inv.method = method;
		inv.callee = callee;
		
		inv.childParams = inv.params;
		
		/**
		 * Make some changes here to instead apply the requested properties
		 */
		Shuffle s = new Shuffle();
		for(Variable v : inv.childParams)
		{
			try
			{
				v.value = s.apply(v.value);
			}
			catch(IllegalArgumentException ex)
			{
				//DO nothing, this property just wasn't applicable
			}
		}
		invocations.put(retId, inv);
		Socket childToServer = null;
		try {
			childToServer = mgr.getAClientSocket();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		inv.childRemoteId = mgr.registerInvocation(inv);
		int pid = Forker.fork();
		System.out.println("PID " + pid);
		if (pid == 0)
		{
			//This is the child
			mgr.isChild = true;
			MethodInvocation childResult = new MethodInvocation();
			childResult.childRemoteId = inv.childRemoteId;
			Object[] childParams = new Object[inv.childParams.length];
			for(int i = 0;i<inv.childParams.length;i++)
			{
				childParams[i]=inv.childParams[i].value;
			}
			try {
				childResult.childReturnValue= inv.method.invoke(callee, childParams);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch(Exception ex)
				{
					childResult.childThrownExceptions = ex;
				}

				try{
				mgr.sendToParent(childResult, childToServer);
				Forker.exit();
//				Runtime.getRuntime().halt(0);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
		}
		else
		{
			//This is the main process
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
		synchronized (inv) {
			while(inv.childRemoteId != 0)
			{
				inv.wait();
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


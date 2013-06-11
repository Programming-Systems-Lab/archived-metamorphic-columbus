package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.columbia.cs.psl.invivo.runtime.AbstractLazyCloningInterceptor;
import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;
import edu.columbia.cs.psl.metamorphic.struct.MetamorphicMethodInvocation;

/**
 * Each intercepted object will have its _own_ Interceptor instance. That
 * instance will stick around for the lifetime of the intercepted object.
 * 
 * NB if you want to keep a list of these Interceptors somewhere statically, you
 * probably want to use a WeakHashMap so as to not create memory leaks
 * 
 * @author jon
 * 
 */
public class Interceptor extends AbstractLazyCloningInterceptor {
	private HashMap<Integer, MetamorphicMethodInvocation> invocations = new HashMap<Integer, MetamorphicMethodInvocation>();
	private Class<?> testerClass;
	private Logger logger = Logger.getLogger(Interceptor.class);

	public Interceptor(Object intercepted) {
		super(intercepted);
		System.out.println("Created interceptor");
		try {
			testerClass = Class.forName(intercepted.getClass().getCanonicalName() + "_tests");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Integer invocationID = 0;

	@Override
	public int onEnter(Object callee, Method method, Object[] params) {
		if (isChild(callee))
			return -1;
		int ret = 0;
		synchronized (invocationID) {
			invocationID++;
			ret = invocationID;
		}
		// Create a new invocation object to store
		final MetamorphicMethodInvocation inv = new MetamorphicMethodInvocation();
		inv.setParams(params);
		inv.setMethod(method);
		inv.setCallee(callee);
		inv.orig_params = deepClone(params); // Used for the check method, in
												// case you care to refer to
												// them in the rule

		// Find the rules
		Rule[] rules = method.getAnnotation(Metamorphic.class).rules();

		// Create the arrays needed for invoking methods

		Class<?>[] checkTypes = new Class[params.length + 2]; // First two
																// params will
																// be the two
																// return values
		checkTypes[0] = method.getReturnType();
		checkTypes[1] = method.getReturnType();

		Class<?>[] childTestParamTypes = new Class[params.length + 3];
		// Last three params will be the callee and the method and the original result
		
		childTestParamTypes[params.length] = callee.getClass();
		childTestParamTypes[params.length + 1] = Method.class;
		childTestParamTypes[params.length + 2] = method.getReturnType();
		Object[] childParams = new Object[params.length + 3];
		for (int i = 0; i < params.length; i++) {
			childTestParamTypes[i] = params[i].getClass();
			childParams[i] = inv.orig_params[i];
			checkTypes[i + 2] = params[i].getClass();
		}

		Object calleeClone = deepClone(inv.getCallee());
		inv.setChildren(new MetamorphicMethodInvocation[rules.length]);
		for (int i = 0; i < rules.length; i++) {
			inv.getChildren()[i] = new MetamorphicMethodInvocation();
			inv.getChildren()[i].setParent(inv);
			inv.getChildren()[i].setCallee(shallowClone(calleeClone));
			((MetamorphicMethodInvocation) inv.getChildren()[i]).rule = rules[i];
			try {
				inv.getChildren()[i].setMethod(getMethod(inv.getMethod().getName() + "_" + i,
						childTestParamTypes, testerClass));
				inv.getChildren()[i].setCheckMethod(getMethod(inv.getMethod().getName() + "_Check" + i,
						checkTypes, testerClass));
			} catch (SecurityException e1) {
				logger.error("Error looking up method/check method for " + inv.getMethod().getName() + "_" + i, e1);
			} catch (NoSuchMethodException e1) {
				logger.error("Error looking up method/check method for " + inv.getMethod().getName() + "_" + i, e1);
			}
			inv.getChildren()[i].setParams(deepClone(childParams));
			inv.getChildren()[i].getParams()[params.length] = inv.getChildren()[i].getCallee();
			inv.getChildren()[i].getParams()[params.length + 1] = method;
			inv.getChildren()[i].setThread(createChildThread(inv.getChildren()[i]));
		}

		invocations.put(ret, inv);

		return ret;
	}

	private static ThreadGroup tg = new ThreadGroup("Columbus2Verifier");
	public void onExit(Object val, int op, int id) {
		if (id < 0)
			return;
		MetamorphicMethodInvocation inv = invocations.remove(id);
		inv.setReturnValue(val);
		Thread backgroundChecker = new Thread(tg,getRunnableFor(inv));
		backgroundChecker.start();
	}

	private Runnable getRunnableFor(final MetamorphicMethodInvocation primaryMethodInvocation) {
		return new Runnable() {

			@Override
			public void run() {
				Object origReturnValCloned = deepClone(primaryMethodInvocation.getReturnValue());
				for (MethodInvocation i : primaryMethodInvocation.getChildren()) {
					i.getParams()[i.getParams().length - 1] = origReturnValCloned;
					i.getThread().start();
				}
				try {
					for (MethodInvocation i : primaryMethodInvocation.getChildren()) {
						i.getThread().join();
					}
					Object[] checkParams = new Object[primaryMethodInvocation.getParams().length + 2];
					for (int i = 0; i < primaryMethodInvocation.getParams().length; i++)
						checkParams[i + 2] = primaryMethodInvocation.orig_params[i];
					for (MethodInvocation i : primaryMethodInvocation.getChildren())
					{
						i.getThread().join();
						checkParams[0] = primaryMethodInvocation.getReturnValue();
						checkParams[1] = i.getReturnValue();
						if (((Boolean) i.getCheckMethod().invoke(null, checkParams)) == false)
						{
							throw new IllegalStateException("Metamorphic property has been violated on " + primaryMethodInvocation.getMethod() + ". Rule: [" + ((MetamorphicMethodInvocation) i).rule + "]. Outputs were ["
									+ prettyPrint(primaryMethodInvocation.getReturnValue()) + "], [" + prettyPrint(i.getReturnValue()) + "]");
						}
					}
					System.out.println("Invocation result: " + primaryMethodInvocation);

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		};
	}

	protected String prettyPrint(Object returnValue) {
		if(returnValue.getClass().isArray())
			if(returnValue.getClass().getComponentType().equals(Integer.TYPE))
				return Arrays.toString((int[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Byte.TYPE))
				return Arrays.toString((byte[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Short.TYPE))
				return Arrays.toString((short[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Character.TYPE))
				return Arrays.toString((char[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Boolean.TYPE))
				return Arrays.toString((boolean[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Long.TYPE))
				return Arrays.toString((long[]) returnValue);
			else if(returnValue.getClass().getComponentType().equals(Double.TYPE))
				return Arrays.toString((double[]) returnValue);
			else
				return Arrays.deepToString((Object[]) returnValue);
		return returnValue.toString();
	}
}

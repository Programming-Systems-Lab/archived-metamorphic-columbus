package edu.columbia.cs.psl.metamorphic.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.objectweb.asm.Type;

import edu.columbia.cs.psl.metamorphic.runtime.visitor.InterceptingClassVisitor;

public abstract class AbstractInterceptor {
	private Object interceptedObject;
	
	public AbstractInterceptor(Object intercepted)
	{
		this.interceptedObject = intercepted;
	}
	
	public Object getInterceptedObject() {
		return interceptedObject;
	}
	public abstract int onEnter(Object callee, Method method, Object[] params);
	
	public abstract void onExit(Object val, int op, int id);
	
	protected void setChild(Object obj, boolean val)
	{
		try {
			obj.getClass().getField(InterceptingClassVisitor.IS_CHILD_FIELD).setBoolean(obj, val);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void setAsChild(Object obj)
	{
		try {
			obj.getClass().getField(InterceptingClassVisitor.IS_CHILD_FIELD).setBoolean(obj, true);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected boolean isChild(Object callee)
	{
		if(callee == null || callee.getClass().equals(Class.class))
			return false;
		try {
			return callee.getClass().getField(InterceptingClassVisitor.IS_CHILD_FIELD).getBoolean(callee);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public final int __onEnter(String methodName, String[] types, Object[] params, Object callee)
	{
		return onEnter(callee, getCurMethod(methodName,types), params);
	}

	private Method getCurMethod(String methodName,String[] types)
	{
		try {
			for(Method m : interceptedObject.getClass().getMethods())
			{
				boolean ok = true;
				if(m.getName().equals(methodName))
				{
					Class[] mArgs = m.getParameterTypes();
					if(mArgs.length != types.length)
						break;
					for(int i = 0;i<mArgs.length;i++)
						if(!mArgs[i].getName().equals(types[i]))
							ok = false;

					if(ok)
						return m;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
}

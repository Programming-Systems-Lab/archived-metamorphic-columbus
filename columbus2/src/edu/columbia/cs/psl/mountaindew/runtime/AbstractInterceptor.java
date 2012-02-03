package edu.columbia.cs.psl.mountaindew.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.objectweb.asm.Type;

public abstract class AbstractInterceptor {
	private Object interceptedObject;
	
	public AbstractInterceptor(Object intercepted)
	{
		this.interceptedObject = intercepted;
	}
	
	public Object getInterceptedObject() {
		return interceptedObject;
	}
	public abstract void onEnter(Method method, Object[] params);
	
	public abstract void onExit(Object val, int op);
	
	public final void __onEnter(String methodName, String[] types, Object[] params)
	{
		onEnter(getCurMethod(methodName,types), params);
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

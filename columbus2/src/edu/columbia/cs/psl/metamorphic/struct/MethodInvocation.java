package edu.columbia.cs.psl.metamorphic.struct;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MethodInvocation  implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2038681047970130055L;
	public Object callee;
	public Method method;
	public Object[] params;
	public Object returnValue;
	public Exception thrownExceptions;
	public int remoteId;
	public Thread thread;
	public MethodInvocation[] children;
	public Method testMethod;
	public Method checkMethod;
	
	@Override
	public String toString() {
		String paramStr = "";
		if(params != null)
		for(Object v : params)
		{
			if(v != null)
			paramStr += v.toString();
		}
		String childStr = "";
		if(children != null)
			for(MethodInvocation i : children)
				childStr += i.toString() +",";
		return "[Invocation on method "+ (method == null ? "null" : method.getName()) + " with params " + paramStr + " returning " + returnValue +" on object " + callee +".  Children: ["+childStr+"] ]";
	}
}
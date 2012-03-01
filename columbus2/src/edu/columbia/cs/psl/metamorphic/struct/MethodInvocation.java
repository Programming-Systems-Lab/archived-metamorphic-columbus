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
	public Variable[] params;
	public Object returnValue;
	public Exception thrownExceptions;
	
	public Thread childThread;
	public Variable[] childParams;
	public Object childReturnValue;
	public Exception childThrownExceptions;
	public int childRemoteId;
	
	@Override
	public String toString() {
		String paramStr = "";
		if(params != null)
		for(Variable v : params)
		{
			if(v != null)
			paramStr += v.toString();
		}
		return "[Invocation on method "+ (method == null ? "null" : method.getName()) + " with params " + paramStr + " returning " + returnValue +" on object " + callee +". Child params = " + childParams + ", child return = "+childReturnValue+" ]";
	}
}
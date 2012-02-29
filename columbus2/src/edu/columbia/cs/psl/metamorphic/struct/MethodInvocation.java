package edu.columbia.cs.psl.metamorphic.struct;

import java.lang.reflect.Method;

public class MethodInvocation
{
	public Object callee;
	public Method method;
	public Variable[] params;
	public Object returnValue;

	public Thread childThread;
	public Variable[] childParams;
	public Object childReturnValue;
	@Override
	public String toString() {
		String paramStr = "";
		for(Variable v : params)
		{
			if(v != null)
			paramStr += v.toString();
		}
		return "[Invocation on method "+ method.getName() + " with params " + paramStr + " returning " + returnValue.toString() +" on object " + callee +". Child params = " + childParams + ", child return = "+childReturnValue+" ]";
	}
}
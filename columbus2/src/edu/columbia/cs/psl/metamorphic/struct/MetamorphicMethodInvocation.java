package edu.columbia.cs.psl.metamorphic.struct;


import edu.columbia.cs.psl.invivo.struct.MethodInvocation;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;

public class MetamorphicMethodInvocation extends MethodInvocation
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2038681047970130055L;
	
	public Object[] orig_params;
	public Rule rule;
	
	@Override
	public String toString() {
		String paramStr = "";
		if(getParams() != null)
		for(Object v : getParams())
		{
			if(v != null)
			paramStr += v.toString();
		}
		String childStr = "";
		if(getChildren() != null)
			for(MethodInvocation i : getChildren())
				childStr += i.toString() +",";
		return "[Invocation on method "+ (getMethod() == null ? "null" : getMethod().getName()) + " with params " + paramStr + " returning " + getReturnValue() +" on object " + getCallee() +".  Children: ["+childStr+"] ]";
	}
}
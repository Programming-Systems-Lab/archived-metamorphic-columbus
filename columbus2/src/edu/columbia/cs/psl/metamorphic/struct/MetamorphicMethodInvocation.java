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
package edu.columbia.cs.psl.metamorphic.struct;

import java.io.Serializable;

public class Variable implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4288820679532450913L;
	public Object value;
	public String name;
	public int position;
	
	@Override
	public String toString() {
		return "[Variable "+position+"="+value.toString()+"]";
	}
}
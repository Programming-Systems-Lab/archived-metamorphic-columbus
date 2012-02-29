package edu.columbia.cs.psl.metamorphic.struct;

public class Variable
{
	public Object value;
	public String name;
	public int position;
	
	@Override
	public String toString() {
		return "[Variable "+position+"="+value.toString()+"]";
	}
}
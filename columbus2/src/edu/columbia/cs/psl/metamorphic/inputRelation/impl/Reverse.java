package edu.columbia.cs.psl.metamorphic.inputRelation.impl;

import java.lang.reflect.Array;
import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputRelation.ArrayProcessorHelper;


public class Reverse extends ArrayProcessorHelper {

	@Override
	public String getName() {
		return "Array Reverser";
	}

	@Override
	protected void apply(Object o1, Object o2)
	{
		for(int i = 0;i < Array.getLength(o1);i++)
		{
			Array.set(o2, i, Array.get(o1, Array.getLength(o1) - 1 - i));
		}
	}
	@Override
	protected <T> void applyToList(List<T> src, List<T> dest) {
		for(int i = 0; i<((List) src).size();i++)
		{
			((List) dest).add(((List) src).get(((List) src).size() - 1 - i));
		}
	}
}

package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;

import edu.columbia.cs.psl.metamorphic.inputProcessor.ArrayProcessorHelper;


public class Reverse extends ArrayProcessorHelper {

	@Override
	public String getName() {
		return "Array Reverser";
	}

	@Override
	protected void applyBetweenArrays(Object o1, Object o2)
	{
		for(int i = 0;i < Array.getLength(o1);i++)
		{
			Array.set(o2, i, Array.get(o1, Array.getLength(o1) - 1 - i));
		}
	}
	@Override
	protected <T> void applyToList(List<T> src, List<T> dest) {
		for(int i = 0; i<src.size();i++)
		{
			dest.add(src.get(src.size() - 1 - i));
		}
	}
	@Override
	public HashSet<Object[]> getBoundaryDefaultParameters() {
		HashSet<Object[]> ret = new HashSet<Object[]>();
		ret.add(null);
		return ret;
	}
}

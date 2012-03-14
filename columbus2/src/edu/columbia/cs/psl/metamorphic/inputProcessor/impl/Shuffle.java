package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.columbia.cs.psl.metamorphic.inputProcessor.ArrayProcessorHelper;


public class Shuffle extends ArrayProcessorHelper {

	@Override
	public String getName() {
		return "Shuffler";
	}

	@Override
	protected void apply(Object o1, Object o2)
	{
		for(int i = 0;i < Array.getLength(o1);i++)
			Array.set(o2, i, Array.get(o1, i));
		Random r = new Random();
		for(int i = 0;i < Array.getLength(o1);i++)
		{
			int j = r.nextInt(i+1);
			Object temp = Array.get(o2, i);
			Array.set(o2, i, Array.get(o2, j));
			Array.set(o2, j, temp);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected <T> void applyToList(List<T> srcList, List<T> destList) {
		for(int i = 0; i<((List) srcList).size();i++)
			((List) destList).add(((List) srcList).get(i));
		Collections.shuffle(destList);
	}

}

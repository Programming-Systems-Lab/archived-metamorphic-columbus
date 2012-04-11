package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.inputProcessor.AbstractElementProcessor;

public class Negate extends AbstractElementProcessor {


	@SuppressWarnings("unchecked")
	@Override
	protected Object applyToNonListObject(Object o, Object... params) {
		if(Number.class.isAssignableFrom(o.getClass()))
		{
			Double d = 0 - ((Number) o).doubleValue();
			return returnToOriginalType(d, (Class<? extends Number>) o.getClass());
		}
		throw new IllegalArgumentException("Negate undefined for objects");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Negate";
	}
	@Override
	public HashSet<Object[]> getBoundaryDefaultParameters() {
		HashSet<Object[]> ret = new HashSet<Object[]>();
		ret.add(null);
		return ret;
	}
}

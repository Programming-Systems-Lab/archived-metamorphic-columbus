package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.inputProcessor.AbstractElementProcessor;

public class MultiplyByNumericConstant extends AbstractElementProcessor {

	public MultiplyByNumericConstant()
	{
	}
	
	@Override
	public String getName() {
		return "Multiplies by a constant";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object applyToNonListObject(Object o, Object... params) {
		if(Number.class.isAssignableFrom(o.getClass()))
		{
			Double d = ((Number) o).doubleValue() * ((Number) params[0]).doubleValue();
			return returnToOriginalType(d, (Class<? extends Number>) o.getClass());
		}
		throw new IllegalArgumentException("Can't multiply a constant and an object value - param was " + o);
	}
	@Override
	public HashSet<Object[]> getBoundaryDefaultParameters() {
		HashSet<Object[]> ret = new HashSet<Object[]>();
		ret.add(new Object[] {-1,0,1,100,-100,2});
		return ret;
	}
}

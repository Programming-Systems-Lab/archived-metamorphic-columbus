package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import edu.columbia.cs.psl.metamorphic.inputProcessor.AbstractElementProcessor;

public class MultiplyByNumericConstant extends AbstractElementProcessor {

	public MultiplyByNumericConstant()
	{
	}
	
	@Override
	public String getName() {
		return "Multiplies by a constant";
	}

	@Override
	protected Object applyToNonListObject(Object o, Object... params) {
		if(Number.class.isAssignableFrom(o.getClass()))
		{
			Double d = ((Number) o).doubleValue() * ((Number) params[0]).doubleValue();
			return returnToOriginalType(d, (Class<? extends Number>) o.getClass());
		}
		throw new IllegalArgumentException("Can't multiply a constant and an object value - param was " + o);
	}

}

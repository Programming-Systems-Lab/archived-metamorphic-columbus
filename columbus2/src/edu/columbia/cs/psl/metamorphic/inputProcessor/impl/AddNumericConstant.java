package edu.columbia.cs.psl.metamorphic.inputProcessor.impl;

import edu.columbia.cs.psl.metamorphic.inputProcessor.AbstractElementProcessor;

public class AddNumericConstant extends AbstractElementProcessor {

	public AddNumericConstant()
	{
	}
	
	@Override
	public String getName() {
		return "Add a constant";
	}

	@Override
	protected Object applyToNonListObject(Object o, Object... params) {
		if(Number.class.isAssignableFrom(o.getClass()))
		{
			Double d = ((Number) o).doubleValue() + ((Number) params[0]).doubleValue();
			return returnToOriginalType(d, (Class<? extends Number>) o.getClass());
		}
		throw new IllegalArgumentException("Can't add a constant to an object value");
	}

}

package edu.columbia.cs.psl.metamorphic.processor.impl;

import edu.columbia.cs.psl.metamorphic.processor.AbstractElementProcessor;

public class AddNumericConstant extends AbstractElementProcessor {

	private double constant;
	public AddNumericConstant(double constant)
	{
		this.constant = constant;
	}
	
	@Override
	public int apply(int a) throws IllegalArgumentException {
		return (int) (a + constant);
	}

	@Override
	public long apply(long a) throws IllegalArgumentException {
		return (long) (a + constant);
	}

	@Override
	public boolean apply(boolean a) throws IllegalArgumentException {
		throw new IllegalArgumentException("Can't add a constant to a boolean value");
	}

	@Override
	public float apply(float a) throws IllegalArgumentException {
		return (float) (a + constant);
	}

	@Override
	public byte apply(byte a) throws IllegalArgumentException {
		return (byte) (a+constant);
	}

	@Override
	public char apply(char a) throws IllegalArgumentException {
		return (char) (a+constant);
	}

	@Override
	public double apply(double a) throws IllegalArgumentException {
		return a+constant;
	}

	@Override
	public String getName() {
		return "Add a constant";
	}

	@Override
	protected Object applyToNonListObject(Object o) {
		throw new IllegalArgumentException("Can't add a constant to an object value");
	}

}

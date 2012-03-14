package edu.columbia.cs.psl.metamorphic.inputRelation.impl;

import edu.columbia.cs.psl.metamorphic.inputRelation.AbstractElementProcessor;

public class MultiplyByNumericConstant extends AbstractElementProcessor {

	private double constant;
	public MultiplyByNumericConstant(double constant)
	{
		this.constant = constant;
	}
	
	@Override
	public int apply(int a) throws IllegalArgumentException {
		return (int) (a * constant);
	}

	@Override
	public long apply(long a) throws IllegalArgumentException {
		return (long) (a * constant);
	}

	@Override
	public boolean apply(boolean a) throws IllegalArgumentException {
		throw new IllegalArgumentException("Can't multiply a constant and a boolean value");
	}

	@Override
	public float apply(float a) throws IllegalArgumentException {
		return (float) (a * constant);
	}

	@Override
	public byte apply(byte a) throws IllegalArgumentException {
		return (byte) (a * constant);
	}

	@Override
	public char apply(char a) throws IllegalArgumentException {
		return (char) (a * constant);
	}

	@Override
	public double apply(double a) throws IllegalArgumentException {
		return a * constant;
	}

	@Override
	public String getName() {
		return "Multiplies by a constant";
	}

	@Override
	protected Object applyToNonListObject(Object o) {
		throw new IllegalArgumentException("Can't multiply a constant and an object value");
	}

}

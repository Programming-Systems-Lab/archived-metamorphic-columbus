package edu.columbia.cs.psl.metamorphic.processor.impl;

import edu.columbia.cs.psl.metamorphic.processor.AbstractElementProcessor;

public class Negate extends AbstractElementProcessor {


	@Override
	public int apply(int a) throws IllegalArgumentException {
		return 0 - a;
	}

	@Override
	public long apply(long a) throws IllegalArgumentException {
		return 0L - a;
	}

	@Override
	public boolean apply(boolean a) throws IllegalArgumentException {
		return !a;
	}

	@Override
	public float apply(float a) throws IllegalArgumentException {
		return 0f -a;
	}

	@Override
	public byte apply(byte a) throws IllegalArgumentException {
		throw new IllegalArgumentException("Negate undefined for bytes");
	}

	@Override
	public char apply(char a) throws IllegalArgumentException {
		throw new IllegalArgumentException("Negate undefined for chars");
	}

	@Override
	public double apply(double a) throws IllegalArgumentException {
		return 0d-a;
	}
	@Override
	protected Object applyToNonListObject(Object o) {
		throw new IllegalArgumentException("Negate undefined for objects");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Negate";
	}

}

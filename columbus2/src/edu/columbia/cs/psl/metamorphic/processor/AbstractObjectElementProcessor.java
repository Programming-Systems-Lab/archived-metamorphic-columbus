package edu.columbia.cs.psl.metamorphic.processor;

public abstract class AbstractObjectElementProcessor extends
		MetamorphicInputProcessor {

	@Override
	public boolean apply(boolean a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public byte apply(byte a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public char apply(char a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public double apply(double a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public float apply(float a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public int apply(int a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	@Override
	public long apply(long a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public <T> T[] apply(T[] a) throws IllegalArgumentException {
		T[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public int[] apply(int[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");

	}

	public long[] apply(long[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public boolean[] apply(boolean[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public float[] apply(float[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public byte[] apply(byte[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public char[] apply(char[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}

	public double[] apply(double[] a) throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Property not defined for primitives");
	}
}

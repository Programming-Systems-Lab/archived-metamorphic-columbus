package edu.columbia.cs.psl.metamorphic.processor;

import java.lang.reflect.Array;

public abstract class AbstractArrayProcessor implements MetamorphicInputProcessor {
	public abstract String getName();
	
	public <T> T apply(T a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public int apply(int a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public long apply(long a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public boolean apply(boolean a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public float apply(float a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public byte apply(byte a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public char apply(char a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public double apply(double a) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public abstract <T> T[] apply(T[] a) throws IllegalArgumentException;
	public abstract int[] apply(int[] a) throws IllegalArgumentException;
	public abstract long[] apply(long[] a) throws IllegalArgumentException;
	public abstract boolean[] apply(boolean[] a) throws IllegalArgumentException;
	public abstract float[] apply(float[] a) throws IllegalArgumentException;
	public abstract byte[] apply(byte[] a) throws IllegalArgumentException;
	public abstract char[] apply(char[] a) throws IllegalArgumentException;
	public abstract double[] apply(double[] a) throws IllegalArgumentException;
}

package edu.columbia.cs.psl.metamorphic.inputRelation;


/**
 * A metamorphic input processor is a class that provides a functional
 * transformation to some input. Most uses should not directly implement this
 * interface, but rather instead one of the abstract helpers
 * @author jon
 * @see AbstractElementProcessor
 * @see AbstractArrayProcessor
 * @see ArrayProcessorHelper
 */
public abstract class MetamorphicInputProcessor {
	public abstract <T> T apply(T a) throws IllegalArgumentException;
	public abstract int apply(int a) throws IllegalArgumentException;
	public abstract long apply(long a) throws IllegalArgumentException;
	public abstract boolean apply(boolean a) throws IllegalArgumentException;
	public abstract float apply(float a) throws IllegalArgumentException;
	public abstract byte apply(byte a) throws IllegalArgumentException;
	public abstract char apply(char a) throws IllegalArgumentException;
	public abstract double apply(double a) throws IllegalArgumentException;

	public abstract <T> T[] apply(T[] a) throws IllegalArgumentException;
	public abstract int[] apply(int[] a) throws IllegalArgumentException;
	public abstract long[] apply(long[] a) throws IllegalArgumentException;
	public abstract boolean[] apply(boolean[] a) throws IllegalArgumentException;
	public abstract float[] apply(float[] a) throws IllegalArgumentException;
	public abstract byte[] apply(byte[] a) throws IllegalArgumentException;
	public abstract char[] apply(char[] a) throws IllegalArgumentException;
	public abstract double[] apply(double[] a) throws IllegalArgumentException;
	
	public abstract String getName();
	
	public Object[] applyToVariables(Object[] in)
	{
		Object[] ret = new Object[in.length];
		for(int i = 0; i < in.length; i++)
		{
			try
			{
			ret[i] = apply(in[i]);
			}
			catch(IllegalArgumentException ex)
			{
				ret[i]=in[i];
			}
		}
		return ret;
	}
}

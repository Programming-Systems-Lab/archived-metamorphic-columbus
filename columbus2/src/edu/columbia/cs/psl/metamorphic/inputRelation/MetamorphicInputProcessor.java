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
public interface MetamorphicInputProcessor {
	public <T> T apply(T a) throws IllegalArgumentException;
	public int apply(int a) throws IllegalArgumentException;
	public long apply(long a) throws IllegalArgumentException;
	public boolean apply(boolean a) throws IllegalArgumentException;
	public float apply(float a) throws IllegalArgumentException;
	public byte apply(byte a) throws IllegalArgumentException;
	public char apply(char a) throws IllegalArgumentException;
	public double apply(double a) throws IllegalArgumentException;

	public <T> T[] apply(T[] a) throws IllegalArgumentException;
	public int[] apply(int[] a) throws IllegalArgumentException;
	public long[] apply(long[] a) throws IllegalArgumentException;
	public boolean[] apply(boolean[] a) throws IllegalArgumentException;
	public float[] apply(float[] a) throws IllegalArgumentException;
	public byte[] apply(byte[] a) throws IllegalArgumentException;
	public char[] apply(char[] a) throws IllegalArgumentException;
	public double[] apply(double[] a) throws IllegalArgumentException;
	
	public String getName();
}

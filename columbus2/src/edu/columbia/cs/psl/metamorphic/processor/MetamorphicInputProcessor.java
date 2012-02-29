package edu.columbia.cs.psl.metamorphic.processor;


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
}

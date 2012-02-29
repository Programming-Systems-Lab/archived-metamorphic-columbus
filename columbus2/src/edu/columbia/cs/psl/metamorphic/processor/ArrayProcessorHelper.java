package edu.columbia.cs.psl.metamorphic.processor;

import java.util.List;

public abstract class ArrayProcessorHelper extends AbstractArrayProcessor {
	public <T> T[] apply(T[] a) throws IllegalArgumentException 
	{
		T[] newArray = (T[]) a.clone();
		apply(a,newArray);
        return newArray;
	}
	@Override
	public boolean[] apply(boolean[] a) throws IllegalArgumentException
			 {
		boolean[] newArray = (boolean[]) a.clone();
		apply(a,newArray);
        return newArray;

	}

	@Override
	public int[] apply(int[] a) throws IllegalArgumentException
			 {
		int[] newArray = (int[]) a.clone();
		apply(a,newArray);
        return newArray;
	}

	@Override
	public long[] apply(long[] a) throws IllegalArgumentException
			 {
		long[] newArray = (long[]) a.clone();
		apply(a,newArray);
        return newArray;
	}

	@Override
	public float[] apply(float[] a) throws IllegalArgumentException
			 {
		float[] newArray = (float[]) a.clone();
		apply(a,newArray);
        return newArray;
	}

	@Override
	public byte[] apply(byte[] a) throws IllegalArgumentException
			 {
		byte[] newArray = (byte[]) a.clone();
		apply(a,newArray);
        return newArray;
	}

	@Override
	public char[] apply(char[] a) throws IllegalArgumentException
			 {
		char[] newArray = (char[]) a.clone();
		apply(a,newArray);
        return newArray;
	}

	@Override
	public double[] apply(double[] a) throws IllegalArgumentException
			 {
		double[] newArray = (double[]) a.clone();
		apply(a,newArray);
        return newArray;
	}
	public <T> T apply(T a) throws IllegalArgumentException
	{
		if(a instanceof List<?>)
		{
				try {
					T ret = (T) a.getClass().newInstance();
					applyToList((List) a,(List) ret);
					return ret;
				} catch (InstantiationException e) {
					throw new IllegalArgumentException("Requested object doesn't have an accesible constructor", e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("Requested object doesn't have an accesible constructor", e);
				}
		}
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	protected abstract <T> void applyToList(List<T> srcList, List<T> destList);
	protected abstract void apply(Object srcArray, Object destArray);
}

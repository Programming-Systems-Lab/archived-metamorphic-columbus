package edu.columbia.cs.psl.metamorphic.inputProcessor;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * A superclass for metamorphic input processors which typically do not care
 * about the values in the arrays, but only the structure. Implementors need 
 * only provide two methods, one for arrays and one for Lists which are called
 * for any kind of array (primitive or otherwise).
 * @author jon
 *
 */
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
		else if(a.getClass().isArray())
		{
			try {
				T newArray = (T) Array.newInstance(a.getClass().getComponentType(), Array.getLength(a));
				apply(a,newArray);
		        return newArray;
			} catch (Exception e) {
				throw new IllegalArgumentException("This metamorphic processor is only defined for arrays",e);
			}
		}
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	protected abstract <T> void applyToList(List<T> srcList, List<T> destList);
	protected abstract void apply(Object srcArray, Object destArray);
}

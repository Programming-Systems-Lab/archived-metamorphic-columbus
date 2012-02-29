package edu.columbia.cs.psl.metamorphic.processor;

import java.util.List;

public abstract class AbstractElementProcessor implements
		MetamorphicInputProcessor {
	public abstract String getName();

	@SuppressWarnings("unchecked")
	public <T> T apply(T a) throws IllegalArgumentException {
		if(a instanceof List<?>)
		{
			try {
				T ret = (T) a.getClass().newInstance();
				for(Object o : (List) a)
					((List) ret).add(applyToNonListObject(o));
				return ret;
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Requested object doesn't have an accesible constructor", e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Requested object doesn't have an accesible constructor", e);
			}
		}
		else if(a instanceof Integer)
			return (T) Integer.valueOf(apply(((Integer) a).intValue()));
		else if(a instanceof Boolean)
			return (T) Boolean.valueOf(apply(((Boolean) a).booleanValue()));
		else if(a instanceof Byte)
			return (T)  Byte.valueOf(apply((( Byte) a).byteValue()));
		else if(a instanceof Character)
			return (T) Character.valueOf(apply(((Character) a).charValue()));
		else if(a instanceof Double)
			return (T) Double.valueOf(apply(((Double) a).doubleValue()));
		else if(a instanceof Float)
			return (T) Float.valueOf(apply(((Float) a).floatValue()));
		else if(a instanceof Long)
			return (T) Long.valueOf(apply(((Long) a).longValue()));
		return (T) applyToNonListObject(a);
	}
	
	protected abstract Object applyToNonListObject(Object o);
	
	public <T> T[] apply(T[] a) throws IllegalArgumentException
			 {
		T[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public int[] apply(int[] a) throws IllegalArgumentException
			 {
		int[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;

	}

	public long[] apply(long[] a) throws IllegalArgumentException
			 {
		long[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public boolean[] apply(boolean[] a) throws IllegalArgumentException
			 {
		boolean[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public float[] apply(float[] a) throws IllegalArgumentException
			 {
		float[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public byte[] apply(byte[] a) throws IllegalArgumentException
			 {
		byte[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public char[] apply(char[] a) throws IllegalArgumentException
			 {
		char[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}

	public double[] apply(double[] a) throws IllegalArgumentException
			 {
		double[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}
}

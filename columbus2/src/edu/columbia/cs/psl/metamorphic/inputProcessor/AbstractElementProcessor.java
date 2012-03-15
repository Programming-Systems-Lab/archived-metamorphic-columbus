package edu.columbia.cs.psl.metamorphic.inputProcessor;

import java.util.List;

/**
 * Provides a superclass for metamorphic input processors that apply only
 * to specific elements (rather than to lists or arrays). For convenience,
 * this abstract class will process all array and list inputs by calling
 * the unary apply on each element of each list.
 * 
 * This abstract class will also convienently unbox and rebox primitive types,
 * so there is no need to specifically handle Integer, Float, etc. For this purpose,
 * do not override T apply, rather, override T applyToNonListObject
 * @author jon
 *
 */
public abstract class AbstractElementProcessor extends
		MetamorphicInputProcessor {
	public abstract String getName();

	public final int[] apply(int[] a, Object... parms)
	{
		System.out.println("Hit it at int");
		return a;
	}
	@SuppressWarnings("unchecked")
	public final <T> T apply(T a, Object... params) throws IllegalArgumentException {
		if(a.getClass().isArray())
		{
			return (T) apply((T[]) a, params);
		}
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
		return (T) applyToNonListObject(a,params);
	}
	
	protected abstract Object applyToNonListObject(Object o, Object... params);
	
	public <T> T[] apply(T[] a, Object... params) throws IllegalArgumentException
			 {
		T[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}
	
	public Number returnToOriginalType(Number n, Class<? extends Number> clazz)
	{
		if(clazz.equals(Double.class) || clazz.equals(Double.TYPE))
			return n.doubleValue();
		if(clazz.equals(Integer.class) || clazz.equals(Integer.TYPE))
			return n.intValue();
		if(clazz.equals(Short.class) || clazz.equals(Short.TYPE))
			return n.shortValue();
		if(clazz.equals(Float.class) || clazz.equals(Float.TYPE))
			return n.floatValue();
		if(clazz.equals(Byte.class) || clazz.equals(Byte.TYPE))
			return n.byteValue();
		if(clazz.equals(Long.class) || clazz.equals(Long.TYPE))
			return n.longValue();
		return null;
	}
}

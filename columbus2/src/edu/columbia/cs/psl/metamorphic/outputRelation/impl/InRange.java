package edu.columbia.cs.psl.metamorphic.outputRelation.impl;

import edu.columbia.cs.psl.metamorphic.outputRelation.AbstractOutputRelation;

public class InRange extends AbstractOutputRelation {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Usage: applies([value to see if in range], [lower bound], [upperbound])
	 */
	public static boolean applies(Object v, Object v1, Object v2) {
		
		if(Number.class.isAssignableFrom(v.getClass()) && Number.class.isAssignableFrom(v1.getClass()) &&  Number.class.isAssignableFrom(v2.getClass()))
		{
			return (((Number) v).doubleValue() >= ((Number) v1).doubleValue()) && (((Number) v).doubleValue() <=((Number) v2).doubleValue());
		}
		else if(Comparable.class.isAssignableFrom(v.getClass()) && Comparable.class.isAssignableFrom(v1.getClass()) &&  Comparable.class.isAssignableFrom(v2.getClass()))
		{
			return ((Comparable) v).compareTo(v1) >= 0 && ((Comparable) v).compareTo(v2) <= 0;
		}
		throw new IllegalArgumentException("in range expects 3 arguments, all comparable or of the same numeric type");
	}


}

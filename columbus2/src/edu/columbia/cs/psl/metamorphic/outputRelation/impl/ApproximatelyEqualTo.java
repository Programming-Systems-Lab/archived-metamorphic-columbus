package edu.columbia.cs.psl.metamorphic.outputRelation.impl;

import edu.columbia.cs.psl.metamorphic.outputRelation.AbstractOutputRelation;

public class ApproximatelyEqualTo extends AbstractOutputRelation {
	
	public static boolean applies(Number v1, Number v2, Number difference)
	{
		return Math.abs(v1.doubleValue() - v2.doubleValue()) < difference.doubleValue();
	}
}

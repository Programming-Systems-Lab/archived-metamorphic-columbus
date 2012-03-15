package edu.columbia.cs.psl.metamorphic.outputRelation.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import edu.columbia.cs.psl.metamorphic.outputRelation.AbstractOutputRelation;

public class ValueIn extends AbstractOutputRelation {

	public static boolean applies(Object a1, Object a2) {
		if(a2.getClass().isArray())
		{
			for(int i = 0; i<Array.getLength(a2); i++)
			{
				if( (a1 == null && Array.get(a2,i) == null) || (a1 != null && a1.equals(Array.get(a2, i)) ))
					return true;
			}
			return false;
		}
		else
		{
			for(Object o : ((Collection<?>) a2))
			{
				if( (a1 == null && o == null) || (a1 != null && o.equals(a1)))
					return true;
			}
			return false;
		}
	}
}

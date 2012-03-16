package edu.columbia.cs.psl.metamorphic.inputProcessor;

public abstract class AbstractObjectElementProcessor extends
		MetamorphicInputProcessor {
	public <T> T[] apply(T[] a) throws IllegalArgumentException {
		T[] ret = a.clone();
		for (int i = 0; i < ret.length; i++)
			ret[i] = apply(a[i]);
		return ret;
	}
}

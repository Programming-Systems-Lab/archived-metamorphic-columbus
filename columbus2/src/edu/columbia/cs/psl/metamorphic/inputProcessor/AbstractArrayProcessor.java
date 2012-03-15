package edu.columbia.cs.psl.metamorphic.inputProcessor;

/**
 * A superclass for metamorphic input processors that process ONLY array types, providing
 * the convenience of automatically throwing exceptions for non-array types. Note that 
 * implementors will probably prefer to also handle List types, for which you'd need to override
 * the T apply(T a) definition too.
 * 
 * Most implementors who provide just a simple transformation on each array element and don't
 * care about element type can use the {@link ArrayProcessorHelper}
 * @author jon
 *
 */
public abstract class AbstractArrayProcessor extends MetamorphicInputProcessor {
	
	public <T> T apply(T a, Object... args) throws IllegalArgumentException
	{
		throw new IllegalArgumentException("This metamorphic processor is only defined for arrays");
	}
	public abstract <T> T[] apply(T[] a, Object... args) throws IllegalArgumentException;
}

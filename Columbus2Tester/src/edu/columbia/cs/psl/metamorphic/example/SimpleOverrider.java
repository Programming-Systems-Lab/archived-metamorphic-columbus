package edu.columbia.cs.psl.metamorphic.example;

public class SimpleOverrider extends SimpleExample {
	@Override
	public int findClosestValue(int[] values, int target)
	{
		return -10;
	}
}

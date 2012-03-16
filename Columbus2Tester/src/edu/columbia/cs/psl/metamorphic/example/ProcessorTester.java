package edu.columbia.cs.psl.metamorphic.example;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class ProcessorTester {
    public static void main(String[] args) {
        ProcessorTester tester = new ProcessorTester();
        System.out.println(tester.findClosestValue(new int[] {1,2,3,1000, 10,1000,40000}, 3));
        System.out.println(tester.pickFirstString(new String[] {"a","b","c","b","c","b","c","b","c","b","c","b","c","b","c"}));
    }
    @Metamorphic(rule={"findClosestValue(\\MultiplyByNumericConstant(values, 10), target * 10) == \\result * 10"//})
			,"findClosestValue(\\AddNumericConstant(values, 10), target + 10) == \\result + 10"})
  private int findClosestValue(int values[], int target)
  {
	int distance = 1000000; // start off with a really large distance
	int closestIndex = -1; // the index of the element that is closest to the target

	for (int i = 0; i < values.length; i++)
	{
	    // check the difference between the values and compare it to the distance
	    if (Math.abs(values[i] - target) < distance)
	    {
		// if it's closer, update the distance
		distance = Math.abs(values[i] - target);
		closestIndex = i;
	    }
	}

	
	// this will give an error if the array is empty, but whatever... =)
	return values[closestIndex];
  }
    @Metamorphic(rule={"pickFirstString(\\Shuffle(in)) == \\result"})
    private String pickFirstString(String[] in)
	{
		return in[0];
	}
}

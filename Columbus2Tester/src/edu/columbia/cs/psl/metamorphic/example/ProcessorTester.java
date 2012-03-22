package edu.columbia.cs.psl.metamorphic.example;

import edu.columbia.cs.psl.invivo.runtime.AbstractInterceptor;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;

@Metamorphic
public class ProcessorTester {
    public static void main(String[] args) {
        ProcessorTester tester = new ProcessorTester();
        System.out.println(tester.findClosestValue(new int[] {1,2,3,1000, 10,1000,40000}, 3));
        System.out.println(tester.pickFirstString(new String[] {"a","b","c","b","c","b","c","b","c","b","c","b","c","b","c"}));
        SimpleExample ex2 = new SimpleExample();
        ex2.go(new String[] {"d"});
    }
    @Metamorphic(rules = {
    		@Rule(test ="findClosestValue(\\MultiplyByNumericConstant(values, 10), target * 10)", check = "\\result * 10"),
    		@Rule(test ="findClosestValue(\\AddNumericConstant(values, 10), target + 10)", check = "\\result + 10")
    }
    	)
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
    
//    private static String[] myStuff = new String[] {"d","e","f"};
//    private static String[] myStuff_2 = new String[] {"d","e","f"};
    @Metamorphic(rules = {
    		@Rule(test = "pickFirstString(\\Shuffle(in))", check="\\result")})
    private String pickFirstString(String[] in)
	{
    	try
    	{
    		int childThread = AbstractInterceptor.getThreadChildId();
    		System.out.println("I'm in child " + childThread);
    	}
    	catch(IllegalStateException ex)
    	{
    		System.out.println("I'm in a parent");
    	}
		return in[0];
	}
}

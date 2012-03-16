package edu.columbia.cs.psl.metamorphic.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Rule;

@Metamorphic
public class SimpleExample implements Cloneable{
	public SimpleExample() {
		somethingAnnoying = new HashSet<ArrayList<SimpleExample>>();
		somethingAnnoying.add(new ArrayList<SimpleExample>());
		somethingAnnoying.iterator().next().add(this);
	}
	  /*@
    @meta findClosestValue(\multiply(values, 10), target * 10) == \result * 10
    @meta findClosestValue(\add(values, 10), target + 10) == \result + 10
    @meta findClosestValue(\shuffle(values), target) == \result
  */
	 @Metamorphic(rules = {
	    		@Rule(test ="findClosestValue(\\MultiplyByNumericConstant(values, 10), target * 10)", check = "\\result * 10"),
	    		@Rule(test ="findClosestValue(\\AddNumericConstant(values, 10), target + 10)", check = "\\result + 10", checkMethod = ">=")
	    }
	    	 )
  public int findClosestValue(int[] values, int target)
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

//	@Metamorphic(rule="billBob(\\suffle(in3) == \"def\"")
	public String go(String[] in3)
	{
		if(in3.length > 0)
			return in3[0];
		else
			return "Your array is empty. Phooey!";
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleExample().go(new String[] {"First","Second","Third","Fourth"}));
//		System.out.println(SimpleExample.findClosestValue(new int[] {1,2,3,4,1000,10000,30000}, 300));
//		int[] values = new int[] {1,32,34};
//		int target = 3;
		SimpleExample s = new SimpleExample();
		System.out.println(s.findClosestValue(new int[] {1,2,3,4,100,1000,2000,40000,10000}, 100));
//		
//		
//		for(Method m : SimpleExample.class.getMethods())
//		{
//			System.out.println(m);
//			for(Annotation a : m.getAnnotations())
//			{
//				System.out.println(a);
//			}
//		}
//		SimpleExample.findClosestValue(null, target);
	}
	public Collection<ArrayList<SimpleExample>> somethingAnnoying;
}

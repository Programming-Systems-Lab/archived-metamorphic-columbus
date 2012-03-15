package edu.columbia.cs.psl.metamorphic.example;

import java.util.ServiceLoader;

import javax.annotation.processing.Processor;

import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

public class ProcessorTester {
    public static void main(String[] args) {
        ServiceLoader<Processor> processors = ServiceLoader.load( Processor.class );
        for ( Processor processor : processors ) { 
            System.out.println( processor.getClass().getSimpleName() + " : " + processor.getClass().getCanonicalName() );
        }
//        new ProcessorTester().foo();
    }
    @Metamorphic(rule={"findClosestValue(\\MultiplyByNumericConstant(values, 10), target * 10) == \\result * 10"//})
			,"findClosestValue(\\AddNumericConstant(values, 10), target + 10) == \\result + 10"})
  private int findClosestValue(int[] values, int target)
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
}

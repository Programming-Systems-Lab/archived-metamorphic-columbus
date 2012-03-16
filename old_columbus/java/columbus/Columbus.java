package columbus;

public class Columbus
{
    /* this should be read from a config file */
    private static final int MAX_TESTS = 4;       // maximum allowable number of tests
    private static final double MAX_OVERHEAD = 0.5; // maximum allowable performance overhead

    public static int currTests = 0; // number of currently executing tests
    public static int totalTests = 0;    // total number of tests run

    private static final double startTime = System.currentTimeMillis(); // the time at which the program started
    public static double testTime = 0; // extra time incurred by launching tests
    private static double totalTime = 0; // total time for which the program has been running
    private static double overhead = 0; // overhead from launching tests

    //private static boolean initialized; // whether or not this component has been initialized
    private static final boolean verbose = true; // whether or not to print debug messages

    private static final double p = 1; // probability of running a test

    public static boolean shouldRunTest(String methodName)
    {
	return Math.random() < p;

	/*

	// only run a test if we're not already doing too many
	if (currTests < MAX_TESTS)
	{
	    // see how long the program has been running
	    totalTime = System.currentTimeMillis() - startTime;
	    if (verbose) System.out.println("Total time: " + totalTime);
	    if (verbose) System.out.println("Test time: " + testTime);
	    overhead = testTime / (totalTime - testTime);
	    if (verbose) System.out.println("Overhead: " + overhead);
	    
	    if (overhead > MAX_OVERHEAD)
	    {
		if (verbose) System.out.println("TOO MUCH OVERHEAD " + overhead);
		return false;
	    }
	      
	    currTests++;
	    totalTests++;
	    if (verbose) System.out.println("OKAY TO RUN A TEST");
	    return true;
	}
	else 
	{
	    if (verbose) System.out.println("TOO MANY TESTS " + currTests);
	    return false;
	}
	*/
    }
}
public class Columbus
{
    /* this should be read from a config file */
    private static final int MAX_TESTS = 1;

    // number of currently executing tests
    public static int currTests = 0;

    // total number of tests run
    public static int totalTests = 0;


    public static boolean shouldRunTest(String methodName)
    {
	// need to check the probability for the given method
	//System.out.println("CurrTests " + currTests);

	if (currTests < MAX_TESTS)
	{
	    currTests++;
	    totalTests++;
	    return true;
	}
	else return false;
    }
}
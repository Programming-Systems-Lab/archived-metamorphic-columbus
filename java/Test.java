public class Test
{
    public static void main(String[] args)
    {
	Test t = new Test();

	for (int i = 0; i < 10000; i++)
	    t.sum(10000);

    }

    public long _sum(long N)
    {
	long sum = 0;
	for (int i = 0; i <= N; i++) sum += i;
	//System.out.println("sum is " + sum);
	return sum;
    }

    public long sum(long N)
    {
	// if not running a test, just call the original function
	if (Columbus.shouldRunTest("Test.sum") == false) return _sum(N);

	//long start = System.currentTimeMillis();
	// fork
	Pipe pipe = new Pipe();
	pipe.create();
	int pid = Forker.fork();

	if (pid == 0)
	{
	    // wait for the result
	    String result = pipe.read();
	    //System.out.println("ready to go: " + result);
	    
	    // now invoke the test
	    try
	    {
		if (!testSum(N, Long.parseLong(result.trim())))
		{
		    // probably include N and result, of course
		    //System.out.println("ERROR!");
		}
		else
		{
		    //System.out.println("Test " + Columbus.totalTests + " passed");
		}
		// kill the process
		Forker.exit();
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
		// kill the process
		Forker.exit();
	    }
	}
	else
	{
	    //long end = System.currentTimeMillis();
	    //System.out.println("Time to fork: " + (end-start));

	    // run the "original" invocation of the method
	    long result = _sum(N);

	    // send a message over the pipe that the original method is done
	    pipe.write(result);

	    // update the counter
	    Columbus.currTests--;

	    // return the result and carry on
	    return result;
	}

	return -1;

    }

    public boolean testSum(long N, long result)
    {
	//System.out.println("starting test");
	if (_sum(2 * N) == 2 * result + N*N)
	    {
		return true;
	    }
	else 
	    {
		//System.out.println(_sum(2*N) + " " + (2*result+N*N));
		return false;
	    }
    }
}
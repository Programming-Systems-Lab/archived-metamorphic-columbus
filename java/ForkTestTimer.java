public class ForkTestTimer
{
    public static void main(String[] args)
    {
	ForkTestTimer test = new ForkTestTimer();
	test.go();
    }

    public void go()
    {

	try
	{
	    // do the fork
	    long start = System.currentTimeMillis();
	    int pid = Forker.fork();
	    long end = System.currentTimeMillis();
	    System.out.println("fork: " + (end-start));

	    //System.out.println(pid);
	    if (pid == 0)
	    {
		// this is the child
		System.out.println("CHILD");
	    }
	    else
	    {
		System.out.println("PARENT " + pid);
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
	

    }
}

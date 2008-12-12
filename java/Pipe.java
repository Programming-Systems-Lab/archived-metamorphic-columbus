public class Pipe
{
    public static int tests = 0;

    // declaration of native methods
    private native int createPipe();
    private native int writePipe(long val);
    private native String readPipe();

    public void create()
    {
	createPipe();
    }

    public int write(long val)
    {
	return writePipe(val);
    }

    public String read()
    {
	return readPipe();
    }

    // load the library
    static 
    {
	System.loadLibrary("pipe");
    }

    /*
    public static void main(String[] args)
    {
	Pipe p = new Pipe();
	p.createPipe();
	int pid = Forker.fork();
	if (pid == 0)
	{
	    System.out.println("Child running tests or whatever");
	    int time = (int)(Math.random() * 1000);
	    try {Thread.sleep(time);} catch (Exception e) { }
	    // when finished, the child writes to the pipe
	    p.write();
	    System.out.println("Child done");
	    Forker.exit();
	}
	else
	{
	    tests++;
	    PipeReader pr = new PipeReader(p);
	    pr.start();
	    System.out.println("Parent keeps going; tests = " + tests);
	    for (int i = 0; i < 100; i++)
		Sorter.testBubbleSort();

	    // just hang out for a bit - we need to do this
	    // so that the first one has a chance to do its read
	    try {Thread.sleep(100);} catch (Exception e) { }

	    // now do it again!!
	    Pipe p1 = new Pipe();
	    p1.createPipe();
	    pid = Forker.fork();
	    if (pid == 0)
	    {
		System.out.println("Second child doing its thing");
		int time = (int)(Math.random() * 1000);
		try {Thread.sleep(time);} catch (Exception e) { }
		p1.write();
		System.out.println("Second child done");
		Forker.exit();
	    }
	    else
	    {
		tests++;
		PipeReader pr1 = new PipeReader(p1);
		pr1.start();
		System.out.println("Parent keeps going again; tests = " + tests);
		for (int i = 0; i < 100; i++)
		    Sorter.testBubbleSort();		
	    
		System.out.println("Done tests=" + tests);
	    }
	}
	
    }
    */
}


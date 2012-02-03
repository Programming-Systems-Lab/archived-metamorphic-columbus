import java.util.*;
import java.io.*;
import java.net.*;

import columbus.Forker;

public class ForkTest2
{
    public static void main(String[] args)
    {
	ForkTest2 test = new ForkTest2();
	test.go();
    }

    public void go()
    {

	try
	{
	    ServerSocket server = new ServerSocket(7234);

	    Forker forker = new Forker();

	    // do the fork
	    int pid = forker.fork();

	    //System.out.println(pid);
	    if (pid == 0)
	    {
		// this is the child
		System.out.println("CHILD");

		Socket s = server.accept();
		System.out.println("Child accepts");
	    }
	    else
	    {
		System.out.println("PARENT " + pid);

		Socket s = server.accept();
		System.out.println("Parent accepts");
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
	

    }
}

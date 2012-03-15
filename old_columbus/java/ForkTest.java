import java.util.*;
import java.io.*;
import java.net.*;

import columbus.Forker;

public class ForkTest
{
    public static void main(String[] args)
    {
	ForkTest test = new ForkTest();
	//test.go();
	test.loop();
    }


    public void loop()
    {
	for (int i = 0; i < 8150; i++)
	{
	    System.out.println("parent " + i);

	    int pid = Forker.fork();
	    if (pid == 0)
	    {
		System.out.println("child " + i);
		Forker.exit();
	    }
	}
	
    }

    public void go()
    {
	try
	{
	    Forker forker = new Forker();

	    // do the fork
	    int pid = forker.fork();

	    //System.out.println(pid);
	    if (pid == 0)
	    {
		try
		{
		    // this is the child
		    System.out.println("CHILD");

		    PrintWriter out = new PrintWriter(new File("foo.txt"));
		    System.out.println("Created PrintWriter");

		    out.println("Hello world!!!");
		    out.flush();
		    System.out.println("done writing");

/*
		    Socket connect = new Socket("localhost", 1234);
		    System.out.println("created a socket");

		    // get the input stream
		    Scanner in = new Scanner(connect.getInputStream());	    
		    // get the output stream
		    PrintWriter out = new PrintWriter(connect.getOutputStream());

		    out.write("5\n");
		    out.flush();
		    System.out.println("wrote");

		    System.out.println(in.nextLong());

		    out.close();
		    */

		}
		finally
		{
		    System.out.println("About to call EXIT");
		    forker.exit();
		    System.exit(0);
		    System.out.println("Still... alive");
		}
	    }
	    else
	    {
		System.out.println("PARENT");
	    }

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    System.exit(0);
	}
	
	try { Thread.sleep(1000); } catch (Exception e) { }

    }
}

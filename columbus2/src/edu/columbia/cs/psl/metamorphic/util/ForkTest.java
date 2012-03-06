package edu.columbia.cs.psl.metamorphic.util;
import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;

import edu.columbia.cs.psl.metamorphic.ipc.IPCManager;


public class ForkTest
{

	    
    public static void main(String[] args)
    {
	ForkTest test = new ForkTest();
	//test.go();
	try{
	test.loop();
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
    }
  
    private int flag = 0;
    public void loop() throws Exception
    {
	    System.out.println("parent "  + flag);
	    
	    IPCManager mgr = IPCManager.getInstance();
	    Socket clientSock = mgr.getAClientSocket();
	    
	    int pid = Forker.fork();


	    if (pid == 0)
	    {
	    	mgr.isChild = true;
	    	flag = 3;
	    	System.out.println("child " + flag);
		
	    	flag = 10;
	    	try {
//			Thread.currentThread().sleep(100);
			String newData = "New String to write to file..." + System.currentTimeMillis();
			mgr.sendToParent(newData,clientSock);
			System.out.println("Closed channel");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Forker.exit();
	    }
	    else
	    {

	    	System.out.println("Parent pid: " + pid);
	    	flag = 1;
	    	System.out.println("Set flag");
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

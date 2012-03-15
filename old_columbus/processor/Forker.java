/**
 * This class is responsible for invoking a UNIX fork 
 * method through JNI.
 */

public class Forker 
{
    // declaration of native method
    private static native int doFork();

    private static native void doExit();

    /**
     * This method calls the native doFork method and returns
     * the process id of this particular process. The child will
     * have a pid of 0, the parent's will be unchanged.
     */
    public static int fork()
    {
	// call the native method
	int pid = doFork();

	/**
	System.out.println("Got the pid " + pid);

	// if the pid is 0, then you're the child
	if (pid == 0)
	    System.out.println("Child " + pid);
	else
	    System.out.println("Parent " + pid);
	**/

	return pid;

    }

    /**
     * Used to exit the current process.
     */
    public static void exit()
    {
	doExit();
	suicide();
    }

    private static void suicide()
    {
	//System.out.println("DEAD");
	try
	{
	    byte[] b = new byte[100];
	    String[] cmd = { "bash", "-c", "echo $PPID" };
	    Process p = Runtime.getRuntime().exec(cmd);
	    p.getInputStream().read(b);
	    String pid = new String(b).trim();

	    // now kill this process
	    String[] cmd2 = { "kill", "-9", pid };
	    p = Runtime.getRuntime().exec(cmd2);
	    //System.out.println("STILL ALIVE!");

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }


    // load the library
    static 
    {
	System.loadLibrary("forker");
    }

    /**
     * Main method just used for testing.
     */
    public static void main(String[] args) 
    {
	new Forker().fork();
    }

}

/**
 * This class contains static methods for setting a process' affinity for
 * certain processors, i.e. explicitly stating which processor a process
 * will run on. It does so by using underlying native methods.
 */

public class Affinity
{
    // declaration of native methods
    private static native int setAffinityN(int cpu);
    private static native int checkAffinityN(int cpu);


    // load the library
    static 
    {
	System.loadLibrary("affinity");
    }

    /**
     * This method determines whether or not the current process is set
     * to run on the given CPU.
     */
    public static boolean checkAffinity(int cpu)
    {
	return (checkAffinityN(cpu) == 1);
    }

    /**
     * This method sets the current process' affinity for the specified CPU.
     * The return value is whether or not the operation succeeded. Note that
     * currently this only allows for setting the process to run on ONE CPU.
     */
    public static boolean setAffinity(int cpu)
    {
	return (setAffinityN(cpu) == 0);
    }

    /**
     * Main method just used for testing.
     */
    public static void main(String[] args) 
    {
	int cpu = Integer.parseInt(args[0]);

	Affinity a = new Affinity();

	System.out.println("Setting affinity to " + cpu);
	if (a.setAffinity(cpu)) System.out.println("success");

	//while (true);

	System.out.println("checkAffinity(0) = " + a.checkAffinity(0));
	System.out.println("checkAffinity(1) = " + a.checkAffinity(1));
	System.out.println("checkAffinity(2) = " + a.checkAffinity(2));
	System.out.println("checkAffinity(3) = " + a.checkAffinity(3));
	System.out.println("checkAffinity(4) = " + a.checkAffinity(4));
	System.out.println("checkAffinity(5) = " + a.checkAffinity(5));
	
    }

}

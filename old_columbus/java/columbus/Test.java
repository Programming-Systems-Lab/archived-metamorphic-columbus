package columbus;
public class Test
{
    public static void main(String[] args)
    {
	Test t = new Test();

	long[] numbers = new long[10000];
	for (int i = 0; i < numbers.length; i++)
	    numbers[i] = (long)(Math.random() * 1000000);

	System.out.println(t.sum(numbers));

    }

    /*@
	@meta sum(permute(N)) == \result;
	@meta testSum(N, result) == true;
    */
    public long __sum(long[] N)
    {
	long sum = 0;
	for (int i = 0; i < N.length; i++) sum += N[i];
	//System.out.println("sum is " + sum);
	return sum;
    }
    
public long sum (long[] N)  {
 long result = __sum(N);
 if (columbus.Columbus.shouldRunTest("Test.sum")) {
   int pid = columbus.Forker.fork();
   if (pid == 0) {
      columbus.Affinity.setAffinity(1);
      if (__testSum(N, result) == false) {
           System.out.println("TEST FAILED: sum ");
      }
      else {
           //System.out.println("test passed");
      }
      columbus.Forker.exit();
   }
 }
return result;
}

 public synchronized boolean __testSum(long[] N, long result) {
    try {
        if (!(columbus.RuleProcessor.approximatelyEqualTo(__sum(permute(N)), result))) return false;
        if (!(columbus.RuleProcessor.approximatelyEqualTo(testSum(N, result), true))) return false;
        return true;
    }
    catch (Exception e) { e.printStackTrace(); }
    finally {
    }
    return false;
 }


    public boolean testSum(long[] N, long result)
    {
	for (int i = 0; i < N.length; i++) N[i] *= 2;
	return __sum(N) == result * 2;
    }


}

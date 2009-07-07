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
      @meta sum(permute(N)) == \result
      @meta testSum(N, result) == true
    */
    public long sum(long[] N)
    {
	long sum = 0;
	for (int i = 0; i < N.length; i++) sum += N[i];
	//System.out.println("sum is " + sum);
	return sum;
    }

    public boolean testSum(long[] N, long result)
    {
	for (int i = 0; i < N.length; i++) N[i] *= 2;
	return __sum(N) == result * 2;
    }


}
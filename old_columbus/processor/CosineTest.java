public class CosineTest
{
    public static void main(String[] args)
    {
	//Test t = new Test();
	
	for (double x = 0; x < Math.PI * 2; x += 0.01)
	{
	    //t.cosine(x);
	    System.out.println(x);
	    if (Math.cos(x) != Math.cos(x + Math.PI * 2)) 
	       System.out.println(x + ": " + Math.cos(x) + " " + Math.cos(x + Math.PI * 2));
	}
    }
}
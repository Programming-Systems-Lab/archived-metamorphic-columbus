public class Convert
{
    public static String convert(String line)
    {
	String[] parts = line.split("==");

	if (parts.length == 2)
	{
	    String left = parts[0].trim();
	    String right = parts[1].trim();
	    
	    return "columbus.RuleProcessor.approximatelyEqualTo(" + left + ", " + right + ")";
	}
	else return line;
    }


    public static void main(String[] args)
    {
	System.out.println(Convert.convert("a == b"));
	System.out.println(Convert.convert("foo.bar(1, 2) == result * 2"));
	System.out.println(Convert.convert("a >= b"));
    }


}
package edu.columbia.cs.psl.metamorphic.example;

import edu.columbia.cs.psl.metamorphic.runtime.Interceptor;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class SimpleExample {
	@Metamorphic
	public String go(String in,String in2, String[] in3, int x)
	{
		String foobar = "x";
		int foo = 10;
		int bar=200;
		System.out.println("Param length in go is " + in3.length);

		return in.toLowerCase();
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleExample().go("abc","def",args,0));
	}
}

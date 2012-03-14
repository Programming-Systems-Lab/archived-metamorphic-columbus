package edu.columbia.cs.psl.metamorphic.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import edu.columbia.cs.psl.metamorphic.runtime.Interceptor;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;

@Metamorphic
public class SimpleExample implements Cloneable{
	private int foo = 0;
	public SimpleExample() {
		somethingAnnoying = new HashSet<ArrayList<SimpleExample>>();
		somethingAnnoying.add(new ArrayList<SimpleExample>());
		somethingAnnoying.iterator().next().add(this);
	}
	
	@Metamorphic(rule="foo")
	public String go(String[] in3)
	{
		if(in3.length > 0)
			return in3[0];
		else
			return "Your array is empty. Phooey!";
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleExample().go(new String[] {"First","Second","Third","Fourth"}));
	}
	public Collection<ArrayList<SimpleExample>> somethingAnnoying;
}

package edu.columbia.cs.psl.metamorphic.example;

import com.rits.cloning.Cloner;

import edu.columbia.cs.psl.metamorphic.runtime.InterceptorClassLoader;
import edu.columbia.cs.psl.metamorphic.util.DeepClone;

public class CloneTest {
	
	public static void main(String[] args) {
		
		InterceptorClassLoader l = new InterceptorClassLoader();
		Thread.currentThread().setContextClassLoader(l);
		try {
			Class<SimpleExample> desired = (Class<SimpleExample>) l.loadClass("edu.columbia.cs.psl.metamorphic.example.SimpleExample");
			Object ex = desired.newInstance();
			Cloner c = new Cloner();
			c.deepClone(ex);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}

package edu.columbia.cs.psl.metamorphic.runtime;




public class MetamorphicInjector {

	public void go(String[] args)
	{
		InterceptorClassLoader l = new InterceptorClassLoader();
		Thread.currentThread().setContextClassLoader(l);
		try {
			
			Class c = l.loadClass(args[0]);
			String[] args2 = new String[args.length-1];
			for(int i = 1; i<args.length;i++)
				args2[i-1] = args[i];
			c.getMethod("main", String[].class).invoke(null, (Object) args2);
			
//			c.getMethod("go",String.class).invoke(c.newInstance(),"zzz");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		SimpleExample.main(args);
	}
	public static void main(String[] args) {
		new MetamorphicInjector().go(args);
	}
}


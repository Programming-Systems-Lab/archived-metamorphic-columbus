package edu.columbia.cs.psl.mountaindew.runtime;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import edu.columbia.cs.psl.mountaindew.runtime.annotation.Metamorphic;
import edu.columbia.cs.psl.mountaindew.runtime.visitor.InterceptingClassVisitor;



public class InterceptorClassLoader extends ClassLoader {
	WeakHashMap<String, Class> loadedClasses = new WeakHashMap<String, Class>();

	static final ClassLoader BASE_CLASS_LOADER = InterceptorClassLoader.class
			.getClassLoader();

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {

		if (!loadedClasses.containsKey(name)) {
			if (!name.startsWith("net.sf.cglib") && !name.startsWith("java")) {
				// name = name + "Enhanced";
				loadedClasses.put(name, super.loadClass(name, resolve));
				loadedClasses.put(name,
						createProxy(super.loadClass(name, resolve)));
			} else
				loadedClasses.put(name, super.loadClass(name, resolve));
		}
		Class c = loadedClasses.get(name);
		return c;
	}

	private Class createProxy(Class c) {
		boolean shouldRewrite = false;
		for(Method m : c.getMethods())
		{
			for(Annotation a : m.getAnnotations())
			{
				if(a.annotationType().equals(Metamorphic.class))
					shouldRewrite = true;
			}
		}
		if(shouldRewrite)
		{
			try {
				ClassReader cr = new ClassReader(c.getName());
				  ClassWriter cw = new ClassWriter(cr,
			     ClassWriter.COMPUTE_MAXS |
			ClassWriter.COMPUTE_FRAMES);
				  InterceptingClassVisitor cv = new InterceptingClassVisitor(cw);
				  cv.setClassName(c.getName());
				  cr.accept(cv, ClassReader.EXPAND_FRAMES);
				  
				Class r = defineClass(c.getName(), cw.toByteArray(), 0, cw.toByteArray().length);
				return r;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return c;
		
	}
}

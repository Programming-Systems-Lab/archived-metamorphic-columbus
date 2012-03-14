package edu.columbia.cs.psl.metamorphic.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.regex.Pattern;

import edu.columbia.cs.psl.metamorphic.runtime.visitor.InterceptingClassVisitor;

public class DeepClone {
	private IdentityHashMap<Object, Object> clonedCache;
	private static HashSet<Class<?>> immutableClasses;
	
	static
	{
		immutableClasses = new HashSet<Class<?>>();
		immutableClasses.add(String.class);
        immutableClasses.add(Integer.class);
        immutableClasses.add(Long.class);
        immutableClasses.add(Boolean.class);
        immutableClasses.add(Class.class);
        immutableClasses.add(Float.class);
        immutableClasses.add(Double.class);
        immutableClasses.add(Character.class);
        immutableClasses.add(Byte.class);
        immutableClasses.add(Short.class);
        immutableClasses.add(Void.class);

        immutableClasses.add(BigDecimal.class);
        immutableClasses.add(BigInteger.class);
        immutableClasses.add(URI.class);
        immutableClasses.add(URL.class);
        immutableClasses.add(UUID.class);
        immutableClasses.add(Pattern.class);

	}
	
	private DeepClone()
	{
		clonedCache = new IdentityHashMap<Object, Object>();
	}
	public <T> T __deepClone(T obj)
	{
		System.out.println(obj);
		if(obj == null)
			return null;
		
		System.out.println(obj.getClass());
		if(immutableClasses.contains(obj.getClass()))
			return obj; //it's immutable so no need to clone
		if(!clonedCache.containsKey(obj))
		{
				try {
					Method clone = obj.getClass().getMethod("clone");
					clone.setAccessible(true);
					clonedCache.put(obj, clone.invoke(obj));
					System.out.println("Used native cloen for " + obj.getClass());
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					// TODO Auto-generated catch block
//					e1.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if(!clonedCache.containsKey(obj))
		{
				try {
					clonedCache.put(obj, obj.getClass().getMethod(InterceptingClassVisitor.CLONE_OVERRIDE_METHOD).invoke(obj));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
		}
		if(!clonedCache.containsKey(obj))
		{
			//OK, everything else failed. do this manually i guess?
			
		}
		Object cloned = clonedCache.get(obj);
		if(cloned != null)
		{
			for(Field f : getAllFields(cloned.getClass()))
			{
				System.out.println("Traversing field " + f);
				try {
					f.set(cloned, __deepClone(f.get(obj)));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return (T) cloned;
	}
	private static HashMap<Class<?>, LinkedList<Field>> fieldCache = new HashMap<Class<?>, LinkedList<Field>>();
	private LinkedList<Field> getAllFields(Class<?> clazz)
	{
		if(!fieldCache.containsKey(clazz))
		{
			LinkedList<Field> fields = new LinkedList<Field>();
			for(Field f : clazz.getDeclaredFields())
			{
				f.setAccessible(true);
				fields.add(f);
			}
			if(clazz.getSuperclass() != null)
				fields.addAll(getAllFields(clazz.getSuperclass()));
			fieldCache.put(clazz, fields);
		}
		return fieldCache.get(clazz);
	}
	public static <T> T deepClone(T object)
	{
		DeepClone c = new DeepClone();
		return c.__deepClone(object);
	}
}

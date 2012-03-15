package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoobarTest_test {
	public boolean foo(String bar, FoobarTest caller, Method ____method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		return ____method.invoke(caller,bar) == "something";
	}
}

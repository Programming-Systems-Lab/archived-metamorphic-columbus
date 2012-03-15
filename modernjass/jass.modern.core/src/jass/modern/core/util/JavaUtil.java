/*
	Copyright (c) 2007 Johannes Rieken, All Rights Reserved
	
	This file is part of Modern Jass (http://modernjass.sourceforge.net/).
	
	Modern Jass is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	Modern Jass is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public License
	along with Modern Jass.  If not, see <http://www.gnu.org/licenses/>.
*/
package jass.modern.core.util;

import jass.modern.NonNull;
import jass.modern.Pure;
import jass.modern.core.compile.ContractJavaCompiler;

import java.lang.reflect.Method;

/**
 * A collection of helper methods being related
 * to the Java world.
 *
 * @author riejo
 */
public class JavaUtil {
	
	/**
	 * 
	 * @see #fixGenericTypeName(String)
	 * @see #fixArrayName(String)
	 * @param className
	 * @return
	 */
	public static String fixJavaTypeName(String className) {
		return fixArrayName(fixGenericTypeName(className));
	}
	
	/**
	 * Transform array type names into internal names. E.g
	 * <ul>
	 * <li> <code>int[]</code> will be <code>[I</code>
	 * <li> <code>String[][]</code> will be <code>[[Ljava.lang.String;</code>
	 * </ul>
	 * @param className
	 * @return
	 */
	public static String fixArrayName(@NonNull String className) {
		if(!className.endsWith("]")) 
			return className;
		
		StringBuilder buffer = new StringBuilder();
		
		int first = -1;
		char[] data = className.toCharArray();
		for (int i = 0; i<data.length; i++) {
			char c = data[i];
			if(c == '[') {
				first = first == -1 ? i : first; 
				buffer.append(c);
			}
		}
		
		if(first == -1)
			return className;
		
		String tmp = className.substring(0, first);
		Primitive p = Primitive.parseString(tmp);
		if(p != null) {
			buffer.append(p.binaryName());
		} else {
			buffer.append("L" + tmp + ";");
		}
		
		return buffer.toString();
	}
	
	/**
	 * Remove the generic signature, as defined in 
	 * source code, from the class name. E.g.
	 * from <code>java.lang.List<String></code>, 
	 * <code>java.lang.List</code> is made.
	 * 
	 * @param className
	 * @return
	 */
	public static String fixGenericTypeName(@NonNull String className) {
		if(!className.endsWith(">"))
			return className;
		
		int index = className.indexOf('<');
		return className.substring(0, index);
	}
	
	/**
	 * 
	 * @param name
	 * @return Return <code>true</code> if the passed name
	 * 	is a valid Java identifier.
	 */
	public static boolean isJavaIdentifier(@NonNull String name) {
		char[] data = name.toCharArray();
		
		if(data.length == 0)
			return false;
		
		if(!Character.isJavaIdentifierStart(data[0]))
			return false;
		
		for(int i = 1; i< data.length; i++) {
			if(! Character.isJavaIdentifierPart(data[i]))
				return false;
		}
		
		return true;
	}

	/**
	 * Checks whether a type is pure. A type is pure if 
	 * it is:
	 * <ul>
	 * <li> a primitive type or the accordant wrapper class
	 * <li> java.lang.String
	 * <li> marked with the {@link Pure} annotation.
	 * </ul>
	 * 
	 * @param type
	 * @return Returns <code>true</code> if the passed type, 
	 * 	denoted by its name, is pure type.
	 */
	public static boolean isPure(@NonNull String type) {
		
		if(type.equals("java.lang.String"))
			return true;
		
		if(Primitive.parseStringFromType(type) != null)
			return true;
		
		Primitive p = Primitive.parseString(type);
		
		if(p == null) { 
			return ContractJavaCompiler.getInstance().forName(
					type).isAnnotationPresent(Pure.class);
			
		} else if(p == Primitive.ARRAY) {
			return false;
			
		} else {
			return true;
		}
	}
	
	public static boolean isPure(@NonNull String type, @NonNull String methodName) {
		if(isPure(type)) {
			return true;
		} else {
			Class<?> cls = ContractJavaCompiler.getInstance().forName(type);
			
			/*
			 * A bit dirty because methods are 
			 * selected by their name and not 
			 * by signature. 
			 */
			Method[] methods = cls.getMethods();
			for (Method method : methods) {
				if(method.getName().equals(methodName) && 
						method.isAnnotationPresent(Pure.class)) {
					return true;
				}
			}
			
			return false;
		}
	}
	
}

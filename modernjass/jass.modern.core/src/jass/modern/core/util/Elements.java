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

import jass.modern.Visibility;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.Modifier;
import jass.modern.core.model.impl.TypeFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.tools.JavaFileObject.Kind;

/**
 * A collection of helper methods to ease working with
 * {@link IElement}s.
 * 
 * @author riejo
 */
public final class Elements {
	
	private static ContractJavaCompiler fContractCompiler;
	
	private static TypeFactory fTypeFactory;
	
	static {
		fContractCompiler = ContractJavaCompiler.getInstance();
		fTypeFactory = new TypeFactory(true);
	}
	
	/**
	 * Pattern which matches a fully qualified Java type name
	 */
	static Pattern PATTERN_TYPENAME = Pattern.compile("((\\w+\\.)*)[a-zA-Z][\\w\\$]*");
	
	/**
	 * 
	 * @param str
	 * @return Returns <code>true</code> if the passed string
	 * 	is a valid Java type name. 
	 */
	public static boolean isTypeName(String str) {
		return PATTERN_TYPENAME.matcher(str).matches();
	}
	
	public static boolean isAbstract(IElement element) {
		boolean _abstract = false;
		_abstract |= element.getModifiers().contains(Modifier.ABSTRACT);
		
		if(!_abstract && element instanceof IType) {
			IType type = (IType) element;
			/*
			 * an interface/annotation must not 
			 * have the 'abstract' modifier. 
			 */
			_abstract |= type.getKind().ordinal() >= IType.Kind.INTERFACE.ordinal();
		}
		
		return _abstract;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends IElement> List<T> filter(Class<T> type, List<IElement> elements){
		LinkedList<T> tmp = new LinkedList<T>();
		for (IElement element : elements) {
			
			if(type.isAssignableFrom(element.getClass())) {
				
				tmp.add((T) element);
			}
		}
		return tmp;
	}

	public static <T extends IElement> T filterFirst(Class<T> type, List<IElement> elements) {
		List<T> list = filter(type, elements);
		if(list.isEmpty())
			return null;
		
		return list.get(0);
	}
	
	/**
	 * Returns those elements form a list of elements which match 
	 * the passed criteria.
	 * 
	 * @see #filter(Class, List)
	 * @param name The name of the element. The use of a simple wildcards 
	 * 	is allowed. E.g. <code>jass.modern.*</code> will match anything
	 * 	starting with that string.
	 * @param type Returned element is of this type, a superclass, or a 
	 * 	superinterface. Can be omitted.
	 * @param elements A list of elements which is searched.
	 * @return Return a list of matching elements. If no element matches
	 * 	an empty list is returned.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IElement> List<T> filter(String name, Class<T> type, 
			List<IElement> elements) {
		
		List<T> list = new LinkedList<T>();
		boolean wildcard = name.trim().endsWith("*");
		String pattern = wildcard ? name.substring(0, name.length() - 1) : name;
		
		for (IElement element : elements) {
			String simpleName = element.getSimpleName();
			if((wildcard ? simpleName.startsWith(pattern) : simpleName.equals(pattern)) && 
					(type == null || type.isAssignableFrom(element.getClass()))) {
				
				list.add((T) element);
			}
		}
		
		return list;
	}
	
	/**
	 * @see #filter(String, Class, List)
 	 * @return The first element of elements matching the passed 
 	 * 	constraints. If none is found <code>null</code> is returned.
	 */
	public static <T extends IElement> T filterFirst(String name, Class<T> type,
			List<IElement> elements) {
		
		List<T> list = filter(name, type, elements);
		if(list.isEmpty())
			return null;
		
		return list.get(0);
	}
	
	/**
	 * Walks up the hierarchy of the passed element
	 * until a {@link IElement#getEnclosingElement() enclosing}
	 * element of type T is found.
	 * 
	 * @param <T> The parent type.
	 * @param target Used to specify the parent type. E.g. IExecutable.class
	 * @param element The element which parents are searched.
	 * @return Return <code>null</code> or an enclosing element of type T
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IElement> T getParent(Class<T> target, IElement element) {
		
		if(element == null)
			return null;
		
		return getParent2(target, element.getEnclosingElement());
	}

	@SuppressWarnings("unchecked")
	private static <T> T getParent2(Class<T> target, IElement element) {
		if(element == null)
			return null;

		if(target.isAssignableFrom(element.getClass()))
			return (T) element;
		
		else if(element instanceof IType)
			return null;
		
		return getParent2(target, element.getEnclosingElement());
	}

	/**
	 * Returns true if the passed element is declared in a 
	 * supertype (either interface or parent class). 
	 * 
	 * @param type
	 * @param element
	 * @return
	 */
	public static boolean overrides(IType type, IExecutable element) {
		if(element.getEnclosingElement() != type || 
				!type.getEnclosedElements().contains(element)) {
			
			throw new IllegalStateException();
		}
		
		Collection<IType> superTypes = getAllSuperTypes(type);
		for (IType superType : superTypes) {
			if(superType.getEnclosedElements().contains(element)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns all member types of the passed {@link IType}.
	 * This included all declared fields, constructors, and
	 * methods and those members declared in any supertype.
	 * Private members of supertypes are omitted, by default.
	 * To exclude further members, one can define a visibility
	 * based filter. 
	 * <br />
	 * <em>Performance note:</em> This method is expensive 
	 * since it might reload all types and relies on the caching
	 * of the {@link TypeFactory}.
	 * 
	 * @param type
	 * @param excludes Exclusion filter based on visibility.
	 * @return
	 */
	public static List<IElement> getAllMembers(IType type, Visibility...excludes) {
		
		// (1) add all elements
		List<IElement> elements = new LinkedList<IElement>();
		elements.addAll(type.getEnclosedElements());
		
		// (2) filter elements which are not on the list
		List<Visibility> excludeList = Arrays.asList(excludes);
		for (Iterator<IElement> iter = elements.iterator(); iter.hasNext() 
				&& !excludeList.isEmpty(); ) {
			
			Visibility visibility = Elements.getVisibility(iter.next());
			if(excludeList.contains(visibility)) {
				iter.remove();
			}
		}
		
		// (3) sort XXX why?
		Collections.sort(elements, new ElementComparator());
		
		try {
			
			// (1) analyze supertype
			String superType = type.getSuperclass();
			if(superType != null) {
				elements.addAll(getAllMembers(fTypeFactory.createType(
						superType, fContractCompiler), Visibility.PRIVATE));
			}
			
			// (2) analye interfaces
			List<String> interfaceTypes = type.getInterfaces();
			for (String interfaceType : interfaceTypes) {
				elements.addAll(getAllMembers(fTypeFactory.createType(
						interfaceType, fContractCompiler)));
			}
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return elements;
	}

	/**
	 * Returns and resolves all supertypes of the passed type.
	 * That are all superclass and interfaces, even those how
	 * are the supertype of a supertype respectivly the superinterfaces
	 * of implemented interfaces.
	 * <br />
	 * <em>Performance note:</em> This method is expensive 
	 * since it might reload all types and relies on the caching
	 * of the {@link TypeFactory}.
	 * 
	 * @param type
	 * @return
	 */
	public static Collection<IType> getAllSuperTypes(IType type) {
		Set<IType> types = new HashSet<IType>();
		
			// (1) analyze supertype
			String superType = type.getSuperclass();
			if(superType != null) {
				try {
					IType tmp = fTypeFactory.createType(superType, fContractCompiler);
					types.addAll(getAllSuperTypes(tmp));
					types.add(tmp);
					
				} catch(IOException e) {
					throw new RuntimeException(String.format("IOException load '%s'. Make sure the classpath is also set using the -Acp option. The current class path is '%s'%n", superType, fContractCompiler.getCurrentClasspath()), e);
				}
			}
			
			// (2) analye interfaces
			List<String> interfaceTypes = type.getInterfaces();
			for (String interfaceType : interfaceTypes) {
				try {
					IType tmp = fTypeFactory.createType(interfaceType, fContractCompiler);
					types.addAll(getAllSuperTypes(tmp));
					types.add(tmp);
					
				} catch(IOException e) {
					throw new RuntimeException(String.format("IOException load '%s'. Make sure the classpath is also set using the -Acp option. The current class path is '%s'%n", interfaceType, fContractCompiler.getCurrentClasspath()), e);
				}
			}
			
		
		return types;
	}
	
	public static Visibility getVisibility(IElement element) {
		Visibility tmp = null;
		for(Modifier modifier : element.getModifiers()) {
			tmp = Visibility.parseVisibility(modifier);
			
			if(tmp == null)
				continue;
			
			switch (tmp) {
			case PUBLIC:
			case PROTECTED:
			case PACKAGE_PRIVATE:
			case PRIVATE:
				return tmp;
			}
		}
		
		return Visibility.PACKAGE_PRIVATE;
	}
	
	public static void setVisibility(IElement element, Visibility visibility) {
		
		element.removeModifier(Modifier.PUBLIC);
		element.removeModifier(Modifier.PROTECTED);
		element.removeModifier(Modifier.PACKAGE_PRIVATE);
		element.removeModifier(Modifier.PRIVATE);

		Modifier mod = visibility.toModifier();
		element.addModifier(mod);
	}
	
	public static URI toURI(IElement element, Kind kind) {
		return toURI(element.getSimpleName(), kind);
	}
	
	public static URI toURI(String typeName, Kind kind) {
		try {
			return new URI("modernjass://" + kind + "/" + typeName + kind.extension);
			
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Return the attribute value with name <code>value</code> and type
	 * <code>T</code>. Note that if the wrong type (T) is passed, <code>null</code>
	 * will be returned which does <em>not</em> mean that no default value 
	 * is present.
	 * 
	 * @see Contracts#getValue(IAnnotation, String, Class)
	 */
	public static <T> T getDefaultValue(IAnnotation annotation, Class<T> type) {
		return getValue(annotation, "value", type);
	}

	/**
	 * 
	 * @param <T>
	 * @param annotation
	 * @param valueName
	 * @param type
	 * @return
	 */
	public static <T> T getValue(IAnnotation annotation, String valueName, Class<T> type){
		IAnnotationValue value = annotation.getValue(valueName);
		return getValue(value, type);
	}

	public static <T> T getValue(IAnnotation annotation, String valueName, Class<T> type, T defaultValue) {
		T value = getValue(annotation, valueName, type);
		if(value == null)
			return defaultValue;
		
		return value;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValue(IAnnotationValue value, Class<T> type) {
		if(value == null)
			return null;
		
		List<Object> valueList = value.getValue();
		if(valueList.isEmpty())
			return null;
		
		if(List.class.isAssignableFrom(type) && type.isInstance(valueList))
			return (T) valueList;
		
		if(valueList.size() != 1)
			return null;
		
		Object firstValue = valueList.get(0);
		if(type.isInstance(firstValue))
			return (T) firstValue;
		
		return null;
	}
}

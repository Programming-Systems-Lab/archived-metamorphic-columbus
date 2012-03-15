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
package jass.modern.core.model.impl;

import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IType;
import jass.modern.core.util.CacheMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.lang.model.element.TypeElement;

import com.sun.mirror.declaration.TypeDeclaration;

/**
 * The TypeFactory creates {@link IType}s from <em>binary files</em>
 * (plain bytecode) or from {@link TypeElement}s. {@link IType}s and 
 * all nested elements reflect the structure of Java types (classes, 
 * interfaces, methods, field, annotation, ...) in Modern Jass.
 * <br /> <br />
 * Internally, all {@link TypeFactory}s share a static cache, so that
 * types don't have to be re-created all the time. Such a cache needs
 * to be handled with care and doesn't make sence all the time. 
 * E.g. when a new compilation process starts, it makes no sense to 
 * recover types from cache because they are likely to be changed. On 
 * the other side, during hierachy analysis caching improves performance
 * because types like <code>java.lang.Object</code> don't have to be created
 * over and over. To deal this, {@link TypeFactory}s can be configured
 * not to use the interal cache (which is default) or use them.
 * <br />
 * The size of the cache is <code>100</code> and it is ordered by
 * the frequence of access. 
 * 
 * @see #useCache()
 * @see #useCache(boolean)
 * @see #cache
 * 
 * @author riejo
 */
public class TypeFactory {

	private static CacheMap<String, IType> cache = new CacheMap<String, IType>(100);
	
	/**
	 * Processes bytecodes
	 */
	private TypeFactoryBytecode fBytecodeFac = new TypeFactoryBytecode();
	
	/**
	 * Processes JavaxLangModels
	 */
	private TypeFactoryJavaxLangModel fJavaxLangModelFac = new TypeFactoryJavaxLangModel();
	
	
	/**
	 * Processes ComSunMirror models
	 */
	private TypeFactoryComSunMirror fComSunMirrorFac = new TypeFactoryComSunMirror();
	
	private boolean fUseCache;
	
	public TypeFactory(boolean useCache) { 
		cache.setEnabled(true);
		useCache(useCache);
	}
	
	public boolean useCache() {
		return fUseCache;
	}

	public void useCache(boolean useCache) {
		fUseCache = useCache;
	}
	
	private IType fromCache(String name) {
		if(!useCache())
			return null;
		
		return cache.get(name);
	}
	
	/**
	 * Creates a {@link IType} instance of the name
	 * class file. The type is generated form its 
	 * bytecodes and must be visible for the system
	 * classloader ({@linkplain ClassLoader#getSystemClassLoader()}).
	 * 
	 * @param name The fully qualified name of a class
	 * @return The {@link IType}-representation of the named 
	 * class.
	 * @throws IOException
	 */
	public IType createType(String name) throws IOException {
		IType type = fromCache(name);
		
		if(type == null) {
			type = fBytecodeFac.createTypeByName(name);
			cache.put(name, type);
		}
		
		return type;
	}
	
	public IType createType(String name, ContractJavaCompiler compiler) throws IOException {
		IType type = fromCache(name);
		
		if(type == null) {
			type = createType(compiler.openStream(name));

//			if(!type.getQualifiedName().equals(name)) {
//				throw new IllegalArgumentException("Wrong class name " + name + " for type " + type);
//			}
			
			return type;
			
		} else {
			return type;
		}
	}
	
	/**
	 * Creates a {@link IType} instance from the bytes
	 * offered by the passed {@link InputStream}.
	 * 
	 * @param in InputStream which delivers the bytecode of 
	 * 	a class.
	 * @return The {@link IType}-representation of the class.
	 * @throws IOException
	 */
	public IType createType(InputStream in) throws IOException {
		IType type = fBytecodeFac.createTypeFrom(in);
		
		if(!cache.containsKey(type.getQualifiedName())) {
			cache.put(type.getQualifiedName(), type);
		}
		
		return type;
	}
	
	public IType createType(String className, byte[] data) throws IOException {
		IType type = fromCache(className);
		
		if(type == null) {
			type = createType(new ByteArrayInputStream(data));
			if(!type.getQualifiedName().equals(className)) {
				throw new IllegalArgumentException("Wrong class name " + 
						className + " for type " + type);
			}
			
			return type;
			
		} else {
			return type;
		}
	}
	
	/**
	 * Creates a {@link IType} instance from the passed
	 * {@link TypeElement} model. The use case for this method
	 * is the combination of Modern Jass and the Annotation
	 * Processing Environment which reflects types through
	 * {@link TypeElement}s. <br />
	 * In constrast to {@link #createType(String)} ad 
	 * {@link #createType(InputStream)}, this method does not
	 * rely on the avialability of the bytecode. 
	 * 
	 * @param element
	 * @return The {@link IType}-representation of 
	 * 	the {@link TypeElement}
	 */
	public IType createType(TypeElement element) {
		String name = element.getQualifiedName().toString();
		IType type = fromCache(name);
		
		if(type == null) {
			type = fJavaxLangModelFac.createTypeFromTypeElement(element);
			cache.put(name, type);
		}
		
		return type;
	}
	
	/**
	 * Creates a {@link IType} instance from the passed
	 * {@link TypeDeclaration} model. This method is used
	 * in combination with an Java5 APT-compliant 
	 * annotation processor. 
	 * <br /><br />
	 * <em>Note that apt is replaced by JSR 269.</em>
	 * 
	 * @param typeDecl
	 * @return
	 */
	public IType createType(TypeDeclaration typeDecl) {
		String name = typeDecl.getQualifiedName();
		IType type = fromCache(name);
		
		if(type == null) {
			type = fComSunMirrorFac.createTypeFromTypeDeclaration(typeDecl);
			cache.put(name, type);
		}
		
		return type;
	}
}

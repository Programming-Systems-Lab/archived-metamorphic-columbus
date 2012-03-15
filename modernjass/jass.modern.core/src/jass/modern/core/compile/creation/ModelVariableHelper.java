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
package jass.modern.core.compile.creation;

import jass.modern.core.compile.transform.ModelVariableTransformer;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IType;
import jass.modern.core.util.Elements;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to keep the information
 * about model-variable type relations. 
 * <br /> <br />
 * It is required 
 * to translate references from model variables into the 
 * (internally assigned) name, e.g. <code>bar$model()</code>.
 * 
 * 
 * @see ModelVariableTransformer
 * @author riejo
 */
public class ModelVariableHelper {
	
	public Set<String> getAllModelVariables(IType type){
		Set<String> modelVariables = new HashSet<String>();
		modelVariables.addAll(getDeclaredModelVariables(type));
		
		Collection<IType> superTypes = Elements.getAllSuperTypes(type);
		for (IType superType : superTypes) {
			modelVariables.addAll(getAllModelVariables(superType));
		}
		
		return modelVariables;
	}

	public Set<String> getAllRepresentsDefinitions(IType key){
		Set<String> represents = new HashSet<String>();
		represents.addAll(getDeclaredRepresentsDefinitions(key));
		
		Collection<IType> superTypes = Elements.getAllSuperTypes(key);
		for (IType superType : superTypes) {
			represents.addAll(getAllRepresentsDefinitions(superType));
		}
		
		return represents;
	}
	
	public Set<String> getDeclaredModelVariables(IType type){
		return getVariables(type, "jass.modern.Model", "jass.modern.ModelDefinitions");
	}

	public Set<String> getDeclaredRepresentsDefinitions(IType type){
		return getVariables(type, "jass.modern.Represents", "jass.modern.RepresentsDefinitions");
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> getVariables(IType type, String name, String containerName){
		Set<String> tmp = new HashSet<String>();
		
		// (1) check for plain annotations
		Collection<IAnnotation> annotations = Elements.filter(name,IAnnotation.class, 
				type.getEnclosedElements());
		addNames(tmp, annotations);
		
		// (2) crack up containers
		IAnnotation annotation = Elements.filterFirst(containerName, IAnnotation.class, 
				type.getEnclosedElements());
		if(annotation == null) 
			return tmp;
		
		annotations = Elements.getDefaultValue(annotation, List.class);
		if(annotations == null)
			return tmp;
		
		addNames(tmp, annotations);
		return tmp;
	}

	private void addNames(Set<String> tmp, Collection<IAnnotation> annotations) {
		
		for (IAnnotation annotation : annotations) {
			IAnnotationValue value = annotation.getValue("name");
			
			if(value == null)
				continue;
			
			String str = Elements.getValue(value, String.class);
			if(str == null)
				continue;
			
			tmp.add(str);
		}
	}

	public Map<String, Class<?>> getAllModelVariables2(IType type){
		HashMap<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// (1) check all supertypes
		Collection<IType> types = Elements.getAllSuperTypes(type);
		for (IType superType : types) {
			tmp.putAll(getDeclaredModelVariables2(superType));
		}

		// (2) the type itself
		tmp.putAll(getDeclaredModelVariables2(type));
		
		return tmp;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getDeclaredModelVariables2(IType type){
		HashMap<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// (1) check for plain annotations
		Collection<IAnnotation> annotations = Elements.filter("jass.modern.Model", IAnnotation.class, 
				type.getEnclosedElements());
		addAnnotation(tmp, annotations);
		
		// (2) crack up containers
		IAnnotation annotation = Elements.filterFirst("jass.modern.ModelDefinitions", IAnnotation.class, 
				type.getEnclosedElements());
		if(annotation == null) 
			return tmp;
		
		annotations = Elements.getDefaultValue(annotation, List.class);
		if(annotations == null)
			return tmp;
		
		addAnnotation(tmp, annotations);
		
		return tmp;
	}

	private void addAnnotation(Map<String, Class<?>> map, Collection<IAnnotation> annotations) {
		
		for (IAnnotation annotation : annotations) {
			IAnnotationValue name = annotation.getValue("name");
			IAnnotationValue type = annotation.getValue("type");
			
			if(name ==  null || type == null)
				continue;
			
			String key = Elements.getValue(name, String.class);
			Class<?> value = Elements.getValue(type, Class.class);
			
			if(key == null || value == null)
				continue;
			
			map.put(key, value);
		}
	}
}

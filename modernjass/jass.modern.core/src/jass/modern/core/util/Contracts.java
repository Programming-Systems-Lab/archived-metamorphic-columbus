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

import jass.modern.Also;
import jass.modern.Invariant;
import jass.modern.InvariantDefinitions;
import jass.modern.SpecCase;
import jass.modern.Visibility;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.desugar.Level1Desugarable;
import jass.modern.core.compile.desugar.Level2Desugarable;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IElement;
import jass.modern.core.model.impl.AnnotationValue;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper class that offers utility methods which
 * are used when working with contract annotations.
 *
 * @author riejo
 */
public class Contracts {

	/**
	 * The annotation used throughout Modern Jass naturally
	 * have a {@link Class}-representation. This methods bridges 
	 * the gap between the {@link IAnnotation}-representation and
	 * the {@link Class}-object.
	 * 
	 * @param annotation
	 * @return Return the {@link Class}-type of the passed
	 * 	annotation. <code>null</code> iff the Class object
	 * 	cannot be loaded.
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Annotation> getClass(IAnnotation annotation) {
		
		if(annotation == null)
			return null;
		
		String className = annotation.getSimpleName();
		Class<? extends Annotation> cls = (Class<? extends Annotation>) 
			ContractJavaCompiler.getInstance().forName(className);
		
		return cls;
	}

	public static IAnnotation newInvariant(String code, Visibility visibility) {
		IAnnotation tmp = new jass.modern.core.model.impl.Annotation(Invariant.class.getName());
		new AnnotationValue(tmp, "value", code);
		new AnnotationValue(tmp, "visibility", visibility);
		
		return tmp;
	}

	@SuppressWarnings("unchecked")
	public static List<IAnnotation> getInvariants(IElement element){
		List<IAnnotation> list = new LinkedList<IAnnotation>();
		IAnnotation invariant = Elements.filterFirst(Invariant.class.getName(), 
				IAnnotation.class, element.getEnclosedElements());
		
		if(invariant != null) {
			list.add(invariant);
		}
		
		IAnnotation invariantDecl = Elements.filterFirst(InvariantDefinitions.class.getName(), 
				IAnnotation.class, element.getEnclosedElements());
		if(invariantDecl != null) {
			List<IAnnotation> value = Elements.getDefaultValue(invariantDecl, List.class);
			list.addAll(value);
		}
		
		return list;
	}
	
	public static void removeInvariants(IElement element) {
		Iterator<IElement> iter = element.getEnclosedElements().iterator();
		while(iter.hasNext()) {
			IElement tmp = iter.next();
			if(tmp.getSimpleName().equals(InvariantDefinitions.class.getName()) ||
					tmp.getSimpleName().equals(Invariant.class.getName())) {
				
				iter.remove();
			}
		}
	}
	
	public static IAnnotation newInvariantDefinitionClause(IAnnotation... invariants) {
		IAnnotation tmp = new jass.modern.core.model.impl.Annotation(InvariantDefinitions.class.getName());
		IAnnotationValue value = new AnnotationValue(tmp, "value");
		
		for (IAnnotation annotation : invariants) {
			if(!annotation.getSimpleName().equals(Invariant.class.getName()))
				throw new IllegalArgumentException(annotation.getSimpleName());
			
			value.addEnclosedElement(annotation);
		}
		
		return tmp;
	}
	
	/**
	 * compare with &#64;SpecCase(<b>signalsPost="false"</b>)
	 * @return
	 */
	public static IAnnotationValue defaultSignalsPost() {
		IAnnotationValue tmp = new AnnotationValue("signalsPost");
		tmp.setValue("false");
		
		return tmp;
	}
	
	public static IAnnotationValue signalsPost(String value) {
		IAnnotationValue tmp = new AnnotationValue("signalsPost");
		tmp.setValue(value);
		
		return tmp;
	}

	/**
	 * compare with &#64;SpecCase(<b>signals=Exception.class</b>)
	 * @return
	 */
	public static IAnnotationValue defaultSignals() {
		IAnnotationValue tmp = new AnnotationValue("signals");
		tmp.setValue(Exception.class);
		
		return tmp;
	}
	
	public static IAnnotation newSpecCase(IAnnotationValue pre, IAnnotationValue post, 
			IAnnotationValue signals, IAnnotationValue signalsPost) {
		
		IAnnotation annotation = new jass.modern.core.model.impl.Annotation(
				SpecCase.class.getName());
		
		if(pre != null && pre.getSimpleName().equals("pre")) {
			annotation.addEnclosedElement(pre);
		}
		if(post != null && post.getSimpleName().equals("post")) {
			annotation.addEnclosedElement(post);
		}
		if(signals != null && signals.getSimpleName().equals("signals")) {
			annotation.addEnclosedElement(signals);
		}
		
		if(signalsPost != null && signalsPost.getSimpleName().equals(
				"signalsPost")) {
			
			annotation.addEnclosedElement(signalsPost);
		}
		
		return annotation;
	}

	@SuppressWarnings("unchecked")
	public static List<IAnnotation> getSpecCases(IElement element){
		List<IAnnotation> list = new LinkedList<IAnnotation>();
		
		List<IAnnotation> tmp = Elements.filter(SpecCase.class.getName(), 
				IAnnotation.class, element.getEnclosedElements());
		if(tmp != null) {
			list.addAll(tmp);
		}
		
		IAnnotation also = Elements.filterFirst(Also.class.getName(), 
				IAnnotation.class, element.getEnclosedElements());
		if(also != null) {
			List<IAnnotation> alsoValue = Elements.getDefaultValue(also, List.class);
			list.addAll(alsoValue);
		}
		
		return list;
	}
	
	
	/**
	 * Remove all &#064;SpecCase and &#064;Also annotations
	 * from the passed element.
	 * 
	 * @param element
	 */
	public static void removeSpecCases(IElement element) {
		Iterator<IElement> iter = element.getEnclosedElements().iterator();
		while(iter.hasNext()) {
			IElement tmp = iter.next();
			if(tmp.getSimpleName().equals(Also.class.getName()) ||
					tmp.getSimpleName().equals(SpecCase.class.getName())) {
				
				iter.remove();
			}
		}
	}
	
	public static IAnnotation newAlsoClause(IAnnotation... specCases) {
		IAnnotation tmp = new jass.modern.core.model.impl.Annotation(Also.class.getName());
		IAnnotationValue value = new AnnotationValue(tmp, "value");
		
		for (IAnnotation annotation : specCases) {
			if(!annotation.getSimpleName().equals(SpecCase.class.getName()))
				throw new IllegalArgumentException(annotation.getSimpleName());
			
			value.addEnclosedElement(annotation);
		}
		
		return tmp;
	}

	public static List<IAnnotation> getFlyweightAnnotations(IElement element, Class<? extends Annotation> level){
		List<IAnnotation> annotations = Elements.filter(IAnnotation.class, element.getEnclosedElements());
		
		Iterator<IAnnotation> iter = annotations.iterator();
		while(iter.hasNext()) {
			IAnnotation next = iter.next();
			if(!isLevel1Flyweight(next)) {
				iter.remove();
			}
		}
		
		return annotations;
	}
	
	
	public static boolean isLevel1Flyweight(IAnnotation annotation) {
		Class<?> cls = getClass(annotation);
		return cls.isAnnotationPresent(Level1Desugarable.class);
	}
	
	public static boolean isLevel2Flyweight(IAnnotation annotation) {
		Class<?> cls = getClass(annotation);
		return cls.isAnnotationPresent(Level2Desugarable.class);
	}
	
	public static ContractTarget getContractTarget(IAnnotation annotation) {
		if(! isLevel1Flyweight(annotation))
			return null;
		
		Class<?> cls = getClass(annotation);
		return cls.getAnnotation(Level1Desugarable.class).value();
	}
	
	public static String getPattern(IAnnotation annotation) {
		Class<? extends Annotation> clazz = Contracts.getClass(annotation);
		if(clazz == null)
			return null;
		
		Level2Desugarable metaAnnotation = clazz.getAnnotation(Level2Desugarable.class);
		if(metaAnnotation == null)
			return null;
		
		return metaAnnotation.pattern();
	}

	public static Class<?>[] getTypes(IAnnotation annotation) {
		Class<? extends Annotation> clazz = Contracts.getClass(annotation);
		if(clazz == null)
			return null;
		
		Level2Desugarable metaAnnotation = clazz.getAnnotation(Level2Desugarable.class);
		if(metaAnnotation == null)
			return null;
		
		return metaAnnotation.types();
	}

	/**
	 * Computes and returns the visibility of the contract. The visibility
	 * is either set as an annotation attribute or is taken from the
	 * annotation target.
	 *  
	 * @see Visibility
	 * @param contract
	 * @param target
	 * @return
	 */
	public static Visibility getContractVisibiliy(IAnnotation contract, IElement target) {
		return getContractVisibiliy(contract.getValue("visibility"), target);
	}
	
	public static Visibility getContractVisibiliy(IAnnotationValue contract, IElement target) {
		Visibility visibility = Elements.getValue(contract, Visibility.class);
		if(visibility != null && visibility != Visibility.TARGET)
			return visibility;
		
		return Elements.getVisibility(target);
	}

}

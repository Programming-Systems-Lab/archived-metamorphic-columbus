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

import jass.modern.core.compile.creation.IContractCreator.ContractTypes;
import jass.modern.core.compile.transform.IAnnotationValueTransformer;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IType;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.ElementScanner;
import jass.modern.core.util.Elements;
import jass.modern.meta.Code;
import jass.modern.meta.Container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * The <code>ContractCreationController</code> manages the
 * creation of contracts. 
 * <br /> <br />
 * It drives the {@link IContractCreator}s
 * <ol>
 * <li> {@link InvariantCreator}
 * <li> {@link SpecCaseCreator}
 * <li> {@link ModelVariableCreator}
 * <li> {@link RepresentsCreator}
 * </ol>
 * Further, it ensured that annotation values like
 * <code>@Result != null</code> have been translated into
 * meaningful Java code.
 *
 * @author riejo
 */
@SuppressWarnings("unchecked")
public class ContractCreationController extends ElementScanner<DiagnosticListener<JavaFileObject>>{
	
	private final Map<String, Set<IContractCreator>> registry = new HashMap<String, Set<IContractCreator>>();

	public ContractCreationController() {	
		initDefaults();
	}
	
	public void initDefaults() {
		register(new InvariantCreator());
		register(new SpecCaseCreator());
		register(new ModelVariableCreator());
		register(new RepresentsCreator());
	}
	
	void register(final IContractCreator contractCreator) {
		ContractTypes annotation = contractCreator.getClass().getAnnotation(
				ContractTypes.class);
		
		Class<? extends Annotation>[] targets = annotation.value();
		for (Class<? extends Annotation> target : targets) {
			
			ensureKey(target.getName()).add(contractCreator);
		}
	}
	
	Collection<IContractCreator> ensureKey(String key){
		Set<IContractCreator> list = registry.get(key);
		if(list == null) {
			list = new HashSet<IContractCreator>();
			registry.put(key, list);
		}
		
		return list;
	}

	/**
	 * This method is the starting point of contract creation 
	 * because it takes an annotation, transforms the specification
	 * expressions, and retrieved the accordant {@link IContractCreator}s
	 * to start them.
	 * 
	 */
	@Override
	public void visit(IAnnotation element, DiagnosticListener<JavaFileObject> param) {
			
		String name = element.getSimpleName();
		Set<IContractCreator> tmp = registry.get(name);
		
		if(tmp != null) {
			
			transform(element, param);
			
			IType type = Elements.getParent(IType.class, element);
			for (IContractCreator contractCreator : tmp) {
				contractCreator.create(element, type, param);
			}
			
		} else {
			/*
			 * enters container annotations
			 */
			super.visit(element, param);
		}
	}

	private void transform(IAnnotation element, DiagnosticListener<JavaFileObject> diagnostics) {
		
		Class<? extends Annotation> annotation = Contracts.getClass(element);
		if(annotation == null)
			return;
		
		// (1) inspect contents of containers
		if(annotation.getAnnotation(Container.class) != null) {
			
			List<IAnnotation> elements = Elements.getDefaultValue(element, List.class);
			for (IAnnotation e : elements) {
				transform(e, diagnostics);
			}
		}
		
		// (2) translate code
		Method[] methods = annotation.getMethods();
		for (Method method : methods) {
			
			IAnnotationValue value = element.getValue(method.getName());
			Code code = method.getAnnotation(Code.class);

			if(value != null && code != null) {
				List<IAnnotationValueTransformer> transformers = initTransformer(code.translator());
				for (IAnnotationValueTransformer transformer : transformers) {
					
					transformer.translate(value, diagnostics);
				}
			}
		}
	}
	
	private List<IAnnotationValueTransformer> initTransformer(Class<? extends IAnnotationValueTransformer>[] classes) {
		List<IAnnotationValueTransformer> list = new ArrayList<IAnnotationValueTransformer>(classes.length);
		
		for (Class<? extends IAnnotationValueTransformer> clazz : classes) {
			try {
				list.add(clazz.newInstance());
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return list;
	}

}

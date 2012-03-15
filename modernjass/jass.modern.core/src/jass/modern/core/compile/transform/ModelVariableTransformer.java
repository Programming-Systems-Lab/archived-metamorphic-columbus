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
package jass.modern.core.compile.transform;

import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.compile.creation.Helper;
import jass.modern.core.compile.creation.ModelVariableHelper;
import jass.modern.core.compile.parser.IElementReference;
import jass.modern.core.compile.parser.SimpleExpressionParser;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.IType;
import jass.modern.core.model.IVariable;
import jass.modern.core.util.Elements;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * Since, internally, model variables are represented as methods,
 * all references to model variables need to be translated. This
 * {@linkplain IAnnotationValueTransformer} performs such a
 * translation. For instance, if a model variables <code>foo</code>
 * exists, the specification
 * <pre>
 * &#064;Pre("foo != null")
 * void m(){ ... }
 * </pre>
 * will be transformed into
 * <pre>
 * &#064;Pre("foo$model() != null")
 * void m(){ ... }
 * </pre>
 *
 * @author riejo
 */
public class ModelVariableTransformer implements IAnnotationValueTransformer {

	public static String SUFFIX = Helper.SEPARATOR + Helper.MODEL + "()";
	
	private SimpleExpressionParser fParser = new SimpleExpressionParser();
	
	private ModelVariableHelper fModelVariableHelper = ContractJavaCompiler.getInstance().getModelVariableHelper();
	
	@Override
	public void translate(IAnnotationValue value, DiagnosticListener<JavaFileObject> diagnostics) {
		
		// (1) retrieve type and model variables
		IType type = Elements.getParent(IType.class, value);
		Set<String> modelVariables = fModelVariableHelper.getAllModelVariables(type);
		
		if(modelVariables.isEmpty())
			return;
		
		// (2) get all references from the code
		String code = Elements.getValue(value, String.class);
		List<IElementReference> references = fParser.parse(code);
		
		// (3) remove all references that belong to method - value might not be a method spec..
		IExecutable method = Elements.getParent(IExecutable.class, value);
		if(method != null) {
			List<IVariable> parameters = method.getParameters();
			for(Iterator<IElementReference> iter = references.iterator(); iter.hasNext(); ) {
				IElementReference tmp = iter.next();
				
				for (IVariable variable : parameters) {
					
					/*
					 * type must be UNKOWN (either local or field)
					 * and names must match
					 */
					if(tmp.getType() == IElementReference.Type.UNKNOWN && 
							tmp.getName().equals(variable.getSimpleName()) ) {
						
						iter.remove();
					}
				}
			}
		}
		
		// (4) replace remaining references with synthetic method
		for (IElementReference reference : references) {
			String name = reference.getName();
			
			if(modelVariables.contains(name)) {
				
				/*
				 * If the reference is a FIELD_ACCESS 'this.' comes first
				 */
				String prefix = reference.getType() == IElementReference.Type.FIELD_ACCESS ? 
						"this." : "";
				
				code = code.replaceAll(prefix + name, 
						Matcher.quoteReplacement(prefix + name + SUFFIX));
				
				/*
				 * remove since a 'replaceAll' has been performed
				 * otherwise multiple replacements might happen...
				 */
				modelVariables.remove(name);
			}
		}

		// (5) finally set the new annotation value
		value.setValue(code);
	}

}

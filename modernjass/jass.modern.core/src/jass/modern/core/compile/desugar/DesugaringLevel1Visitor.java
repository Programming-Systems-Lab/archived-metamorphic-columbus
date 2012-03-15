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
package jass.modern.core.compile.desugar;

import jass.modern.Post;
import jass.modern.Pre;
import jass.modern.SpecCase;
import jass.modern.core.bytecode.contracts.handle.ContractTarget;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IAnnotationValue;
import jass.modern.core.model.IExecutable;
import jass.modern.core.util.Contracts;
import jass.modern.core.util.ElementScanner;
import jass.modern.core.util.Elements;

import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * The level 1 desugaring visitor transforms {@link Pre} and
 * {@link Post} into accordant spec cases. The algorithmn for 
 * level 1 desugaring is simple:
 * <ol>
 * <li>Collect all level 1 annotations (note that they have a
 *  direct counterpart in the {@link SpecCase} annotation)
 * <li>Compose a new {@link SpecCase} from the level 1 annotations
 * <li>Add the new {@link SpecCase} to the accordant method
 * </ol>
 *
 * @author riejo
 */
public class DesugaringLevel1Visitor extends ElementScanner<DiagnosticListener<JavaFileObject>> {


	@Override
	public void visit(IExecutable element, DiagnosticListener<JavaFileObject> param) {
		
		List<IAnnotation> flyweights = Contracts.getFlyweightAnnotations(element, Level1Desugarable.class);
		if(flyweights.isEmpty())
			return;
		
		IAnnotation composedSpec = composeFromFlyweight(flyweights);
		List<IAnnotation> userSpecs = Contracts.getSpecCases(element);
		userSpecs.add(composedSpec);
		
		IAnnotation also = Contracts.newAlsoClause(
				userSpecs.toArray(new IAnnotation[userSpecs.size()]));
		
		/* remove old annotations */
		Contracts.removeSpecCases(element);
		for (IAnnotation flyweight : flyweights) {
			element.removeEnclosedElement(flyweight);
		}
		
		element.addEnclosedElement(also);
	}


	protected IAnnotation composeFromFlyweight(List<IAnnotation> flyweights) {
		
		IAnnotationValue pre = null;
		IAnnotationValue post = null;
		IAnnotationValue signalsPost = null;
		
		for (IAnnotation flyweight : flyweights) {
			String value = Elements.getValue(flyweight.getDefaultValue(), String.class);
			ContractTarget target = Contracts.getContractTarget(flyweight);
			switch (target) {
			case PRE:
				pre = flyweight.getDefaultValue();
				pre.setSimpleName("pre");
				pre.setValue(value);
				break;
				
			case POST:
				post = flyweight.getDefaultValue();
				post.setSimpleName("post");
				post.setValue(value);
				break;
				
			case SIGNALS_POST:
				signalsPost = flyweight.getDefaultValue();
				signalsPost.setSimpleName("signalsPost");
				signalsPost.setValue(value);
				break;
				
			default:
				throw new IllegalStateException();
			}
		}
		
		IAnnotation specCase = Contracts.newSpecCase(pre, post, null, signalsPost);
		return specCase;
	}
}

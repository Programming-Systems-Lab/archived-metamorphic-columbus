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

import static jass.modern.core.util.TypeDescriptors.annotationHelper;
import static jass.modern.core.util.TypeDescriptors.annotationSpecCase;
import jass.modern.Helper;
import jass.modern.SpecCase;
import jass.modern.Visibility;
import jass.modern.core.compile.ContractDiagnostic;
import jass.modern.core.compile.ContractJavaCompiler;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IExecutable;
import jass.modern.core.model.Modifier;
import jass.modern.core.util.Configuration;
import jass.modern.core.util.ElementScanner;
import jass.modern.core.util.Elements;
import jass.modern.core.util.Configuration.Issue;
import jass.modern.core.util.Configuration.Type;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

/**
 * This visitor is used to validate the proper use
 * of annotation. The check performed by this
 * visitor go beyond the normal type checks which
 * are done by the Java compiler. 
 *
 * @author riejo
 */
public class AnnotationUsageValidator extends ElementScanner<DiagnosticListener<JavaFileObject>> {
	
	
	@Override
	public void visit(IAnnotation element, DiagnosticListener<JavaFileObject> param) {
		
		// (1) validate SpecCase
		blameWrongSpecCaseUsage(element, param);
		
		// (2) check the @Helper-annotation
		validateHelperAnnotation(element, param);
		
		super.visit(element, param);
	}
	
	/**
	 * Blame the use of signals without {@link SpecCase#pre()}, 
	 * the combination of {@link SpecCase#post()} and
	 * {@link SpecCase#signalsPost()}, the empty or msg-only
	 * spec-case, and the combination of signalsPost without 
	 * signals.
	 * 
	 * @param annotation
	 * @param diagnostics
	 */
	protected void blameWrongSpecCaseUsage(IAnnotation annotation, 
			DiagnosticListener<JavaFileObject> diagnostics) {
		
		if(!annotationSpecCase.getClassName().equals(annotation.getSimpleName()))
			return;
		
		IExecutable parent = Elements.getParent(IExecutable.class, annotation);
		String pre = Elements.getValue(annotation, "pre", String.class);
		String post = Elements.getValue(annotation, "post", String.class);
		Class<?> signal = Elements.getValue(annotation, "signals", Class.class);
		String signalsPost = Elements.getValue(annotation, "signalsPost", String.class);
		
		if(pre != null && pre.trim().length() == 0) {
			diagnostics.report(new ContractDiagnostic(annotation.getValue("pre"), 
					"Pre must not be empty", Kind.ERROR));
		}
		
		if(post != null && post.trim().length() == 0) {
			diagnostics.report(new ContractDiagnostic(annotation.getValue("post"), 
					"Post must not be empty", Kind.ERROR));
		}
		
		if(signalsPost != null && signalsPost.trim().length() == 0) {
			diagnostics.report(new ContractDiagnostic(annotation.getValue("signalsPost"), 
					"SignalsPost must not be empty", Kind.ERROR));
		}
		
		/*
		 * Check for configurable issues 
		 */
		
		Configuration.Type type;
		
		// check for @SpecCase(signals = IOE.class) ... void m() throws <b>IOE</b>
		type = Configuration.getValue(Issue.SIGNALS_NOT_DECLARED);
		if(type != Type.IGNORE && signal != null) {
			boolean valid = false;
			valid |= RuntimeException.class.isAssignableFrom(signal);

			for (String exception : parent.getExceptions()) {
				valid |= ContractJavaCompiler.getInstance().forName(
						exception).isAssignableFrom(signal);
			}
			
			if(!valid) {
				diagnostics.report(new ContractDiagnostic(annotation.getValue("signal"), 
						signal + " must be declared in the throws clause or assignable from java.lang.RuntimeExeption", 
						type.toDiagnosticsKind() ));
			}
		}
		
		// check @SpecCase( signalsPost="true")
		type = Configuration.getValue(Issue.SIGNALS_POST_WITHOUT_SIGANALS);
		if(type != Type.IGNORE && signal == null && signalsPost != null) {
			diagnostics.report(new ContractDiagnostic(annotation.getValue("signalsPost"), "The attribute " +
			"signalsPost should always be used in conjunction with signals", type.toDiagnosticsKind()));
		}
		
		// check @SpecCase() or @SpecCase
		type = Configuration.getValue(Issue.EMPTY_SPEC_CASE);
		if(type != Type.IGNORE && pre == null && post == null && signal == null && signalsPost == null) {
			diagnostics.report(new ContractDiagnostic(annotation, "This specification " +
			"is useless", type.toDiagnosticsKind()));
		}
		
		// check for @SpecCase( pre = "..", post = "..", signals = ..., signalsPost = "..")
		type = Configuration.getValue(Issue.PARTIAL_SPEC_CASE);
		if(type != Type.IGNORE && (pre == null || post == null || signal == null || signalsPost == null)) {
			diagnostics.report(new ContractDiagnostic(annotation, "The specification " +
			"is not complete", type.toDiagnosticsKind()));
		}
	}
	
	
	/**
	 * Checks that the {@link Helper}-annotation is not used 
	 * with {@link Modifier#PUBLIC public}, protected, or 
	 * package protected. methods.
	 * 
	 * @param annotation
	 * @param diagnostics
	 */
	protected void validateHelperAnnotation(IAnnotation annotation, 
			DiagnosticListener<JavaFileObject> diagnostics) {
		
		if(!annotationHelper.getClassName().equals(annotation.getSimpleName())) 
			return;
		
		IExecutable target = Elements.getParent(IExecutable.class, annotation);
		if(!Elements.getVisibility(target).equals(Visibility.PRIVATE)) {
			
			diagnostics.report(new ContractDiagnostic(annotation, "The " + 
					annotation.getSimpleName() + "-annotation is allowed " +
					"when visibility is private only", Kind.ERROR));
		}
	}
}

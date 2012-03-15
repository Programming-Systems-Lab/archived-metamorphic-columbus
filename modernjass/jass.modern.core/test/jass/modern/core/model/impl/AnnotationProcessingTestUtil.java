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

import jass.modern.core.model.IType;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public class AnnotationProcessingTestUtil {
	
	@SupportedAnnotationTypes("jass.modern.*")
	@SupportedSourceVersion(SourceVersion.RELEASE_6)
	public static final class InternalAnnotationProcessor extends AbstractProcessor {
	
		ElementVisitor visitor;
		Object parameter;
		
		public InternalAnnotationProcessor(ElementVisitor visitor,
				Object parameter) {
			this.visitor = visitor;
			this.parameter = parameter;
		}
	
		@Override
		public boolean process(Set<? extends TypeElement> annotations,
				RoundEnvironment roundEnv) {
			
			if(annotations.isEmpty())
				return false;
			
			Set<TypeElement> elements = getRootElements(annotations, roundEnv);
			for (TypeElement typeElement : elements) {
				typeElement.accept(visitor, parameter);
			}
			return true;
		}
		
		private Set<TypeElement> getRootElements(Set<? extends TypeElement> annotations,
				RoundEnvironment roundEnv) {
	
			HashSet<TypeElement> rootElements = new HashSet<TypeElement>();
			for (TypeElement annotation : annotations) {
				Set<? extends Element> elements = roundEnv
						.getElementsAnnotatedWith(annotation);
				Iterator<? extends Element> iter = elements.iterator();
				if (iter.hasNext()) {
					rootElements.add(getRootElement(elements.iterator().next()));
				}
			}
	
			return rootElements;
		}
	
		private TypeElement getRootElement(Element element) {
	
			// (1) special case if some tries with an package
			if (element instanceof PackageElement) {
				return null;
			}
	
			// (2) check if element is innerclass, otherwise return
			if (element instanceof TypeElement) {
	
				TypeElement type = (TypeElement) element;
				if (type.getEnclosingElement() instanceof PackageElement) {
					return type;
				}
			}
	
			// (3) check enclosing element...
			return getRootElement(element.getEnclosingElement());
		}
	}

	public static void runProcessor(AbstractProcessor processor, String... types) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		
		Iterable<? extends JavaFileObject> cu = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(types));
		CompilationTask task = compiler.getTask(null, fileManager, null, null, null, cu);
		task.setProcessors(Arrays.asList(processor));
		task.call();
	}
	
	public static IType runDefaultProcessor(String type) throws Exception {
		TypeFactoryJavaxLangModel.InternalElementVisitor visitor = new TypeFactoryJavaxLangModel().new InternalElementVisitor();
		runProcessor(new InternalAnnotationProcessor(visitor, null), type);
		return visitor.getType();
	}
}

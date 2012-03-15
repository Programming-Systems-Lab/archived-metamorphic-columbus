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
package jass.modern.core.bytecode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * 
 * @author riejo
 */
public class ModernJassAnnotationSearcher extends EmptyVisitor {

	public static final int FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
		| ClassReader.SKIP_FRAMES;
	
	private Map<String, Boolean> fCache = new HashMap<String, Boolean>();
	
	private boolean fHasModernJassAnnotations = false;

	private boolean _Errors = false;
	private int _Depth = 0;
	
	private String fSuperName;
	private String[] fInterfaces;

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		fHasModernJassAnnotations |= desc.startsWith("Ljass/modern/");
		return null;
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {
		
		fHasModernJassAnnotations |= desc.startsWith("Ljass/modern/");
		return null;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

		fSuperName = superName;
		fInterfaces = interfaces;
		_Depth += 1;
	}

	@Override
	public void visitEnd() {
		_Depth -= 1;

		if(_Depth > 0)
			return;
		
		if(hasModernJassAnnotations() || 
				(fSuperName == null && fInterfaces.length == 0)) {
			
			return;
		}
		
		String superName = fSuperName;
		int len = fInterfaces.length;
		String[] interfaces = new String[len];
		System.arraycopy(fInterfaces, 0, interfaces, 0, len);
		
		try {
			// (1) supertype
			if (superName != null) {
				fHasModernJassAnnotations |= searchInParentType(superName);
			}

			// (2) all interfaces
			for (String _interface : interfaces) {
				fHasModernJassAnnotations |= searchInParentType(_interface);
			}

		} catch (Exception e) {
			_Errors = true;
//			e.printStackTrace();
		}
	}

	private boolean searchInParentType(String name) throws IOException {

		// (1) read from cache
		if(fCache.containsKey(name)) {
			return fCache.get(name);
		}
	
		// (2) read and check type
		ModernJassAnnotationSearcher tmp = new ModernJassAnnotationSearcher();
		new ClassReader(name).accept(tmp, FLAGS);
		boolean annotation = tmp.hasModernJassAnnotations();
		
		// (3) cache result
		fCache.put(name, annotation);
		return annotation;
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		
		_Depth += 1;
		return this;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		_Depth += 1;
		return this;
	}

	public boolean hasModernJassAnnotations() {
		return fHasModernJassAnnotations && !_Errors;
	}

	public void reset() {
		fHasModernJassAnnotations = false;
		_Errors = false;
		_Depth = 0;
	}

}

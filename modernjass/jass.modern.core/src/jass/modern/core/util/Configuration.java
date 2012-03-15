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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic.Kind;

public class Configuration {
	
	public enum Type {
		IGNORE, 
		WARNING,
		ERROR;
		
		public Kind toDiagnosticsKind(){
			
			switch(this) {
				case WARNING: 	return Kind.MANDATORY_WARNING;
				case ERROR: 	return Kind.ERROR;
				default: return null;
			}
		}
	}
	
	public enum Issue {
		EMPTY_SPEC_CASE,
		NON_PURE_USE,
		SIGNALS_POST_WITHOUT_SIGANALS,
		SIGNALS_NOT_DECLARED,
		PARTIAL_SPEC_CASE,
		OLD_OF_NONE_PURE;
		
		public String getIdentifier() {
			return "jass.modern.pref." + name();
		}
	}
	
	public static class Attribute {
		private final Issue fKey;
		private Type fValue;
		
		public Attribute(Issue key, Type value) {
			fKey = key;
			fValue = value;
		}
		
		public Type getValue() {
			return fValue;
		}

		public void setValue(Type value) {
			fValue = value;
		}
		
		public Issue getKey() {
			return fKey;
		}
	}
	
	private static final List<Attribute> defaultConfiguration = Arrays.asList(
			new Attribute(Issue.EMPTY_SPEC_CASE, Type.WARNING),
			new Attribute(Issue.NON_PURE_USE, Type.WARNING),
			new Attribute(Issue.SIGNALS_POST_WITHOUT_SIGANALS, Type.IGNORE),
			new Attribute(Issue.SIGNALS_NOT_DECLARED, Type.ERROR),
			new Attribute(Issue.PARTIAL_SPEC_CASE, Type.IGNORE),
			new Attribute(Issue.OLD_OF_NONE_PURE, Type.WARNING)
	);

	private static List<Attribute> configuration = new LinkedList<Attribute>(defaultConfiguration);
	
	public static Collection<Attribute> getDefaultConfiguration() {
		return Collections.unmodifiableCollection(defaultConfiguration);
	}
	
	public static Type getValue(Issue kind) {
		for (Attribute attribute : configuration) {
			
			if(attribute.getKey() == kind) {
				return attribute.getValue();
			}
		}
		
		return null; 
	}
	
	public static void setValue(Issue kind, Type value) {
		for (Attribute attribute : configuration) {
			
			if(attribute.getKey() == kind) {
				attribute.setValue(value);
			}
		}
	}
	
	public static void setValue(String key, String value) {
		Type type = Type.valueOf(value);
		Issue issue = Issue.valueOf(key.substring(key.lastIndexOf('.') + 1));
		setValue(issue, type);
	}
}

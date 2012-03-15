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

public enum Primitive {
	
	/**
	 * A constant for the array type
	 */
	ARRAY	(Array.class),

	BOOLEAN (Boolean.class),
	CHAR 	(Character.class),
	BYTE 	(Byte.class),
	SHORT 	(Short.class),
	INT 	(Integer.class),
	LONG 	(Long.class),
	FLOAT 	(Float.class),
	DOUBLE 	(Double.class);

	public static final class Array { 	}
	
	private Class<?> fType;
	
	private Primitive(Class<?> type) {
		fType = type;
	}
	
	public static Primitive parseString(String str) {
		
		if(str.endsWith("]"))
			return ARRAY;
		
		for(Primitive p : values()) {
			
			if(p.asPrimitive().equals(str)) {
				return p;
			}
		}
		return null;
	}
	
	public static Primitive parseStringFromType(String type) {
		
		for(Primitive p : values()) {
			if(p.asWrapper() != null && p.asWrapper().equals(type)) {
				return p;
			}
		}
		
		return null;
	}
	
	public String asPrimitive() {
		switch(this) {
		case ARRAY:	return "[]";
		default:	return name().toLowerCase();
		}
	}
	
	public String asWrapper() {
		switch(this) {
		case ARRAY:	return null;
		default:	return getType().getName();
		}
	}
	
	public String binaryName() {
		switch (this) {
		case BOOLEAN: 	return "Z";
		case CHAR: 		return "C";
		case BYTE: 		return "B";
		case SHORT: 	return "S";
		case INT:		return "I";
		case LONG:		return "J";
		case DOUBLE:	return "D";
		case FLOAT:		return "F";
			
		default: return null;
		}
	}
	
	public Class<?> getType(){
		return fType;
	}
}

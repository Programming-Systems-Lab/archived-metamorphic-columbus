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
package jass.modern.tests;

import jass.modern.Invariant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Invariant("map != null")
public class ClassWithGenerics<A extends Number, B, C, D> extends SuperClassWithGenerics<C> implements
	SuperInterfaceWithGenerics1<A, B>, SuperInterfaceWithGenerics2<D>{
	
	public Map<String, Integer> map = new HashMap<String, Integer>();
	
	public <T> T transform(T type) {
		return type;
	}
	
	public <T extends Number> Number multiply(T a, T b, double c) {
		return a.doubleValue() * b.doubleValue() - c;
	}
	
	public A doStuff(List<List<A>> list) {
		return null;
	}
}

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

import jass.modern.Also;
import jass.modern.SpecCase;

import java.util.List;

public class RuntimeDummyBufferWithErrors {
	
	@Also({
		@SpecCase(pre="obj != null"),
		@SpecCase(pre="!isFull"), 
		@SpecCase(post="contains(oj)") })
	public void add(Object obj) {
		
	}
	
	public boolean isFull() {
		return false;
	}
	
	public boolean contains(Object obj) {
		return false;
	}
	
	@Also(
		@SpecCase(signals=NullPointerException.class, signalsPost="true"))
	public List<Object> getAll(){
		return null;
	}
}

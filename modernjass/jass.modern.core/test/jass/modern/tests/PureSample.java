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
import jass.modern.Pure;
import jass.modern.SpecCase;

public class PureSample {
	
	class Bar {
		
		String guess() {
			
			String guess = bar;
			bar = null;
			return guess;
		}
	}
	
	protected String bar;
	
	@Pure boolean foo() {
		return bar.equals(new Bar().guess());
	}
	
	@Also({
		@SpecCase( pre = "data != null", post = "@Result != null && @Result.length != 0"),
		@SpecCase( pre = "data != null && index < 0 || index >= data.length", 
				signals = ArrayIndexOutOfBoundsException.class,
				post = "@Result != null") })
	public String toString(Object[] data, int index) {
		
		return data[index].toString();
	}
	
	
}

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

import jass.modern.SpecCase;

public class SampleClass12 {

	@SpecCase( pre = "true", post = "true")
	void m1() {
		throw new NullPointerException();
	}
	
	@SpecCase( pre = "true", post = "n == 0", 
			signals = NullPointerException.class, 
			signalsPost = "n == 1")
	void m2(int n) {
		
		if(n == 1)
			throw new NullPointerException();
	}
	
	@SpecCase(signalsPost = "true")
	void m3() throws Exception {
		throw new Exception();
	}
}

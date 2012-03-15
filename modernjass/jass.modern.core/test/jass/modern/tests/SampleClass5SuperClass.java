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
import jass.modern.Name;
import jass.modern.Pure;
import jass.modern.SpecCase;
import jass.modern.Visibility;

public class SampleClass5SuperClass {

	@Also( { @SpecCase(pre = "n == 0"), @SpecCase(post = "true"),
			@SpecCase(post = "false", visibility = Visibility.PRIVATE) })
	public void number(int n) {

	}

	@Also( { @SpecCase(pre = "false"),
			@SpecCase(pre = "true", visibility = Visibility.PRIVATE) })
	public void number2(@Name("n")
	int n) {

	}

	@SpecCase(pre = "false", post = "false", visibility = Visibility.PROTECTED)
	public void m3() {

	}

	private int value = 123;
	
	@Also({
		@SpecCase(pre ="value == 123", visibility = Visibility.PRIVATE),
		@SpecCase(pre ="zero() != 0") })
	public void m4() {
		
	}
	
	@Pure public int zero() {
		return 0;
	}
}

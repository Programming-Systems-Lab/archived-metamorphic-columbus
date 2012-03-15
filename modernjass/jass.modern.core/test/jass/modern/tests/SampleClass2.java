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
import jass.modern.Invariant;
import jass.modern.Pre;
import jass.modern.Pure;
import jass.modern.SpecCase;

public class SampleClass2 {

	@Invariant("@ForAll(Object o : obj; o != null)")
	Object[] obj = new Object[0];
	
	
	@SpecCase( pre = "@ForAll(String arg: args; arg != null)")
	public void main(String[] args) {
		
	}
	
	@SpecCase(pre = "n >= 0")
	public int sqrt(int n) {

		return 0;
	}

	public int fCount = 0;

	@Also(
			@SpecCase(pre = "0 <= n && n <= 5", post = "fCount >= n"))
	public void addToCount(int n) {
		fCount += n;
	}

	@Also(@SpecCase(pre = "n % 2 == 0", signals = NullPointerException.class, signalsPost = "fCount == 0"))
	public void mightThrow(int n) {
		throw new NullPointerException();
		
	}
	
	@Also( @SpecCase(pre="Math.abs(n - m) < 5"))
	public void twoNumbers(int n, int m) {
		fCount += (n + m);
	}
	
	int foo$return$value = 2;
	
	@SpecCase( pre = "3 == bar()")
	@Pure public int foo() {
		return foo$return$value;
	}
	
	@Pre("2 == foo()")
	@Pure public int bar() {
		return 3;
	}
}

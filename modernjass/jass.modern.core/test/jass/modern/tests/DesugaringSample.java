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
import jass.modern.Length;
import jass.modern.Max;
import jass.modern.NonNull;
import jass.modern.Post;
import jass.modern.Pre;
import jass.modern.SpecCase;

public class DesugaringSample {
	
	@NonNull String fName;
	
	@Max(100) double fAge;
	
	@Pre("o != null")
	@Post("true")
	void m1(Object o) {
		
	}
	
	@SpecCase( pre="true", post="true")
	@Also( { 
		@SpecCase( pre = "o == null", signals=NullPointerException.class),
		@SpecCase( pre = "len >= 0") })
	@Pre("o != null")
	@Post("false")
	void m2(int len, Object o) {
		if(o == null)
			throw new NullPointerException();
		
	}
	
	void m3(@NonNull Object o) {
		
	}
	
	@Post("len >= 0")
	@NonNull Integer m4(int len, @NonNull Object o) {
		
		return 1;
	}
	
	@SpecCase( post = "false")
	@Also( 
		@SpecCase( pre="o == null", signals = NullPointerException.class))
	void m5(int len, @NonNull Object o) {
		
	}
	
	void m6(@Length(12) int f) { // WRONG SPEC ON PURPOSE @Length cannot be applied to 'int'
		
	}
}

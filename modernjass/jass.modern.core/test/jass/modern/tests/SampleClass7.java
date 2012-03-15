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
import jass.modern.Min;
import jass.modern.NonNull;
import jass.modern.Post;
import jass.modern.Pre;
import jass.modern.Range;
import jass.modern.SpecCase;

public class SampleClass7 {
	
	@Pre("o != null")
	@Post("false")
	void add(Object o) { 
		
	}

	@Pre("len >= 0 && o != null")
	@SpecCase( pre= "o == null", signals = NullPointerException.class)
	void add2(int len, Object o) {
		if(o == null)
			throw new NullPointerException();
	}
	
	void add3(@NonNull Object o) {
		
	}
	
	void add4(@Length(6) String foo) {
		
	}
	
	void add5(@Range(from = 3, to = 5) double d) {
		
	}
	
	void add6(@Min(8) long a) {
		
	}
	
	void add7(@NonNull int a) {
		
	}
	
	void add8a(@NonNull Object o) {
		if(o == null) throw new NullPointerException();
	}
	
	@Pre("o != null")
	@SpecCase(pre = "o == null", signals = NullPointerException.class, signalsPost = "true")
	void add8(Object o) {
		if(o == null) throw new NullPointerException();
	}
	
	@Pre("o != null")
	@SpecCase( pre = "o == null", signals = NullPointerException.class, signalsPost = "false")
	void add8false(Object o) {
		if(o == null) throw new NullPointerException();
	}

	@Also({
		@SpecCase( pre = "o != null"),
		@SpecCase( pre = "o == null", signals = NullPointerException.class, signalsPost = "true") })
	void add8b(Object o) {
		if(o == null) throw new NullPointerException();
	}
	
	@Also({
		@SpecCase( pre = "o != null"),
		@SpecCase( pre = "o == null", signals = NullPointerException.class, signalsPost = "false") })
	void add8bfalse(Object o) {
		if(o == null) throw new NullPointerException();
	}
}

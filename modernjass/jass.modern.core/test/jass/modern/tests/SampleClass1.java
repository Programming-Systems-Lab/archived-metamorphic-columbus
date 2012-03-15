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

class SampleClass1 {
	
	/**
	 * This is javadoc!
	 */
	@SpecCase(pre="false || false", preMsg="foo")
	public void preFails() {	}
	
	@Also( @SpecCase(post="false"))
	public void postFails() {	}
	
	@Also( @SpecCase(pre="true", post="false"))
	public void postFails2() {	}
	
	@Also( {@SpecCase(pre="true"), @SpecCase(post="false")})
	public void postFails3() {	}
	
	@Also( @SpecCase(pre="true"))
	public void preSuccess() {	}
	
	@Also( @SpecCase(post="true"))
	public void postSuccess1() {	}
	
	@Also( @SpecCase(pre="true", post="true"))
	public void postSuccess2() {	}
	
	@Also( {@SpecCase(pre="true"), @SpecCase(post="true")}) 
	public void postSuccess3() {	}
	
	@Also( {@SpecCase(pre="true"), @SpecCase(pre="true", post="true")})
	public void postSuccess4() { 	}
}

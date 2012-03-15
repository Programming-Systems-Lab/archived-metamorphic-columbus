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

/*
 * A buffer with size 1
 */
public class SampleClass3 {
	
	public Object theObjThing;
	
	@SpecCase( pre= "dummy != null")
	public SampleClass3(Object dummy) {
		
	}
	
	@Also({ 
		@SpecCase( pre="obj == null", signals=NullPointerException.class),
		@SpecCase( pre="!isFull() && obj != null", post="contains(obj)") })
	public void add(Object obj) {
		if(obj == null)
			throw new NullPointerException();
		
		theObjThing = obj;
	}
	
		
	@Pure public boolean contains(Object obj) {
		return !isEmpty() && theObjThing.equals(obj);
	}
	
	@Pure public boolean isFull() {
		return theObjThing != null;
	}
	
	@Pure public boolean isEmpty() {
		return theObjThing == null;
	}
	
	@Also(@SpecCase(post="theObjThing == null"))
	public void clear() {
		theObjThing = null;
	}
	
	@Also(@SpecCase( pre = "blas.length == 2", post="true"))
	public static int pass(String[] blas) {
		
		return 12345;
	}
	
	@Also( { 
		@SpecCase( post = "@Result == 12"), 
		@SpecCase( post = "@Return == 12") })
	public int returnConst() {
		return 12;
	}
	
	@Also( @SpecCase( post = "@Return == 1", postMsg = "grr, should return 1"))
	public int returnConst2() {
		return 2;
	}
}

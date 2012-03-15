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
import jass.modern.Model;
import jass.modern.Pure;
import jass.modern.Represents;
import jass.modern.RepresentsDefinitions;
import jass.modern.SpecCase;
import jass.modern.Visibility;
@RepresentsDefinitions({
	@Represents(name = "bla", by = "fInt"), 
	@Represents(name = "foo", by = "1") })
@Model(name = "bla", type = Integer.class)
public class RuntimeDummyBuffer extends RuntimeDummyBufferParent {
	
	@Invariant( value = "fStr == null", visibility = Visibility.PRIVATE)
	public String fStr = null;
	
	@Invariant("fInt == 0")
	private int fInt = 0;
	
	public RuntimeDummyBuffer() {
		super(0, null);
	}
	
	@Also({
		@SpecCase(pre="obj != null", preMsg = "do not add >null<"),
		@SpecCase(pre="!isFull()", signals=Exception.class, signalsPost="obj == null", signalsMsg="foo"),
		@SpecCase(post="contains(obj)", postMsg = "bar")})
	public void add(Object obj) throws Exception {
		System.out.println(">>RETURN<<");
		return;
	}
	
	@Pure
	public boolean isFull() {
		return false;
	}
	
	@Pure
	@Also( {
		@SpecCase(pre="obj != null"),
		@SpecCase(pre="false", visibility = Visibility.PRIVATE) })
	public boolean contains(Object obj) {
		return false;
	}
	
	@Also( @SpecCase( pre="args != null"))
	public static void main(String[] args) {
		
		if(args.length == 1) {
		
			System.out.println("check for >bla<");
			new RuntimeDummyBuffer().contains("bla");
			
			System.out.println("check for >null<");
			new RuntimeDummyBuffer().contains(null);
			
		} else {
			System.out.println("add element >bla<");
			try {
				new RuntimeDummyBuffer().add("bla");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

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

import jass.modern.Helper;
import jass.modern.Invariant;

@Invariant("str.equals(\"abc\")")
public class SampleClass6 extends SampleClass6SuperClass {
		
	public String str = "abc";
	
	@Invariant("fConst == 9")
	int fConst = 9;
	
//	public SampleClass6() {
//		System.out.println("BLA");
//	}
	
	public void doNotChange() {
		
	}
	
	public void doChange() {
		fConst = 12;
	}
	
	public void doChangeInheritedField() {
		fAmount = -1;
	}
	
	public void doChangeInheritedFieldPrivateSpec() {
		fPrivateConst = 12;
	}
	
	@Helper private void helperChanges() {
		fConst = 12;
	}
	
	public void callsHelper() {
		helperChanges();
		fConst = 9;
	}
}

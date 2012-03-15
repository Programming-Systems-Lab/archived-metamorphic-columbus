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
package jass.modern.core.compile;

import jass.modern.SpecCase;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MisUsingAnnotations {
	
	@SpecCase(signals = NullPointerException.class)
	public void m1() {
		
	}
	
	@SpecCase( signals = IOException.class )
	public void m2() {
		
	}
	
	@SpecCase( signals = FileNotFoundException.class )
	public void m3() throws IOException {
		
	}
}

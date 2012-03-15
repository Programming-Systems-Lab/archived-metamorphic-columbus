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
/**
 * 
 */
package jass.modern.tests;

import jass.modern.Invariant;
import jass.modern.Model;
import jass.modern.ModelDefinitions;
import jass.modern.Post;
import jass.modern.Pre;
import jass.modern.Pure;

@ModelDefinitions( {
	@Model(name = "foo", type = Integer.class),
	@Model(name = "bar", type = String.class) 
	})
public abstract class SampleClass9Super {

	public SampleClass9Super() {
		m();
	}
	
	@Invariant("v > foo")
	long v = 1234;
	
	@Pre("value() < foo")
	@Pure int value() {
		return 3;
	}
	
	@Post("@Result != 0")
	abstract int m();
	
}

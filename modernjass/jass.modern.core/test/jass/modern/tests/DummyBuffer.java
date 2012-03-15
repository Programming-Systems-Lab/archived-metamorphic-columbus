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
import jass.modern.Min;
import jass.modern.Model;
import jass.modern.Name;
import jass.modern.NonNull;
import jass.modern.Pre;
import jass.modern.Pure;
import jass.modern.SpecCase;
import jass.modern.Visibility;

import java.lang.reflect.Field;

@Model(name = "data", type = Object[].class)
public class DummyBuffer implements IDummyInterface {
	
	static Field staticField = null;
	
	@Invariant("fNotNullObject != null")
	private Object fNotNullObject;
	
	public int fPrimitiveInt;
	
	@Also({
		@SpecCase(post="fPrimitiveInt == value", signals=IllegalArgumentException.class, visibility = Visibility.PRIVATE),
		@SpecCase(pre="value > 0")
	})
	public DummyBuffer(@Name("value") int value) {
		fPrimitiveInt = value;
	}

	@Pre(value = "obj != null")
	public void add(Object obj) {
		
	}
	
	@Pure
	boolean isFull(@NonNull Object o, @Min(12) int i) 
		throws NullPointerException, IllegalArgumentException {
		
		return false;
	}
	
	@Also( @SpecCase( pre="args != null"))
	protected Object[] main(String[][] args) {
		return args;
	}
	
	@Pre("args.length == 2")
	private static void staticMain(String[] args) {
	
	}
}

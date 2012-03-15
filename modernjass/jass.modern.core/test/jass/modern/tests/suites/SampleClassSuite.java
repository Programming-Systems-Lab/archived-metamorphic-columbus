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
package jass.modern.tests.suites;

import jass.modern.tests.SampleClass10ATest;
import jass.modern.tests.SampleClass10BTest;
import jass.modern.tests.SampleClass10SuperClassTest;
import jass.modern.tests.SampleClass11ImpleTest;
import jass.modern.tests.SampleClass12Test;
import jass.modern.tests.SampleClass13Test;
import jass.modern.tests.SampleClass1Test;
import jass.modern.tests.SampleClass2Test;
import jass.modern.tests.SampleClass3Test;
import jass.modern.tests.SampleClass42Test;
import jass.modern.tests.SampleClass4Test;
import jass.modern.tests.SampleClass5Test;
import jass.modern.tests.SampleClass6Test;
import jass.modern.tests.SampleClass7Test;
import jass.modern.tests.SampleClass8Test;
import jass.modern.tests.SampleClass9Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( value = {
		SampleClass1Test.class,
		SampleClass2Test.class,
		SampleClass3Test.class,
		SampleClass4Test.class,
		SampleClass42Test.class,
		SampleClass5Test.class,
		SampleClass6Test.class,
		SampleClass7Test.class,
		SampleClass8Test.class,
		SampleClass9Test.class,
		SampleClass10SuperClassTest.class,
		SampleClass10ATest.class,
		SampleClass10BTest.class,
		SampleClass11ImpleTest.class,
		SampleClass12Test.class,
		SampleClass13Test.class
})
public class SampleClassSuite {

}

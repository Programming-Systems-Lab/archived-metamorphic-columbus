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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

public class Helper {
	
	public static String[] getAbsoluteFilePath(Class<?>... classes) {
		String[] paths = new String[classes.length];
		String userPath = System.getProperty("user.dir");
		
		for (int i = 0; i < paths.length; i++) {
			paths[i] = userPath + File.separator + "test" + File.separator + 
				classes[i].getName().replace('.', '/') + ".java"; 
		}
		return paths;
	}
	
	public static InputStream openInputStream(Class<?> clazz) {
		
		return new BufferedInputStream(clazz.getClassLoader().getResourceAsStream(
				clazz.getName().replace('.', '/') + ".class"));
	}
}

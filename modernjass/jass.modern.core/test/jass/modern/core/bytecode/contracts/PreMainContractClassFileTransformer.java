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
package jass.modern.core.bytecode.contracts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class PreMainContractClassFileTransformer {
	
	public static void premain(final String args, Instrumentation inst) {
		
		inst.addTransformer(new FilteredTransformer(new ContractClassFileTransformer(true), args.split(",")));
	}
	
	static class FilteredTransformer implements ClassFileTransformer {

		ClassFileTransformer parent;
		List<String> names;
		
		public FilteredTransformer(ClassFileTransformer parent, String... classnames) {
			this.parent = parent;
			names = Arrays.asList(classnames);
		}

		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {
			
			if(filter(className)) {
				byte[] data = parent.transform(loader, className, classBeingRedefined, 
						protectionDomain, classfileBuffer);
				
				System.out.println("TRANSFORMED: " + className);
				dump(className, data);
				return data;
				
			} else
				return classfileBuffer;
		}

		protected boolean filter(String className) {
			className = className.replace('/', '.');
			
			for (String name : names) {
				if(name.equals(className))
					return true;

				if(name.endsWith("*") && 
						className.startsWith(name.substring(0, name.length() - 1)))
					return true;
			}
			
			return false;
		}
		
	}

	public static void dump(String className, byte[] data) {
		File f = new File(System.getProperty("user.home") + File.separator + "MODERNJASS_DUMP" + 
				File.separator + className.replace('.', '/') + ".class");
		f.getParentFile().mkdirs();
		
		try {
			f.delete();
			
			FileOutputStream out = new FileOutputStream(f);
			out.write(data);
			out.flush();
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

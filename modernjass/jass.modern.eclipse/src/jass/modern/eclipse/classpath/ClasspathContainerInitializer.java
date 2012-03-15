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
package jass.modern.eclipse.classpath;

import jass.modern.eclipse.ModernJassPlugin;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ClasspathContainerInitializer extends
		org.eclipse.jdt.core.ClasspathContainerInitializer {

	public class ModernJassContainer implements IClasspathContainer {

		IPath fPath;
		IClasspathEntry[] fEntries;		
		
		public ModernJassContainer(IPath path, IClasspathEntry... classpathEntries) {
			fPath = path;
			fEntries = classpathEntries;
		}
		
		public IClasspathEntry[] getClasspathEntries() {
			return fEntries;
		}

		public String getDescription() {
			return "Modern Jass";
		}

		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		public IPath getPath() {
			return fPath;
		}
	}
	

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		
		IClasspathEntry entry= ModernJassPlugin.getModernJassLibraryEntry();
		IClasspathContainer container= new ModernJassContainer(containerPath, entry);

		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, 	
				new IClasspathContainer[] { container }, null);
	}
}

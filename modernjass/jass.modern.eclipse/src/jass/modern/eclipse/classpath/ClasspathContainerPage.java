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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class ClasspathContainerPage extends NewElementWizardPage implements
		IClasspathContainerPage {
	
	private static final Pattern VERSION_PATTERN = Pattern.compile(".*-(\\d{8}).jar");
	
	public ClasspathContainerPage() {
		super("Modern Jass");
		
		setTitle("Modern Jass Classpath Container");
		setDescription("Add Modern Jass library to classpath");
	}

	public boolean finish() {
		return true;
	}

	public IClasspathEntry getSelection() {
		
		return JavaCore.newContainerEntry(new Path(
				"jass.modern.eclipse.classpathContainerInitializer"));
	}

	public void setSelection(IClasspathEntry containerEntry) {
		
	}

	public void createControl(Composite parent) {
		
		IClasspathEntry entry = ModernJassPlugin.getModernJassLibraryEntry();
		
		if(entry != null) {
			String path = entry.getPath().toOSString();
			String version = null;
			Matcher match = VERSION_PATTERN.matcher(path);
			if(match.matches()) {
				version = match.group(1);
			}

			Group composite = new Group(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			
			new Label(composite, SWT.NONE).setText("Modern Jass version: ");
			new Label(composite, SWT.NONE).setText(version);
			
			new Label(composite, SWT.NONE).setText("Location: ");
			new Label(composite, SWT.NONE).setText(path);
			
			setControl(composite);
			
		} else {
			setErrorMessage("Could not resolve Modern Jass library entry.");
		}
	}

}

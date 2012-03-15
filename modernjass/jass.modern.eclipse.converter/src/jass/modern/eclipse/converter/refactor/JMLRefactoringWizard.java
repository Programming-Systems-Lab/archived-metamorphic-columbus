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
package jass.modern.eclipse.converter.refactor;

import jass.modern.eclipse.converter.ConverterPlugin;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JMLRefactoringWizard extends RefactoringWizard {

	public class JMLWizardUserPage extends UserInputWizardPage {

		public JMLWizardUserPage(String name) {
			super(name);
			setMessage("This feature is experimental", WizardPage.WARNING);
		}

		public void createControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText("Note: This feature does only work for invariants!");
			
			setControl(label);
		}
		
	}
	
	public JMLRefactoringWizard(Refactoring refactoring, int flags) {
		super(refactoring, flags);
		setDefaultPageTitle("Convert JML to Modern Jass");
		setDefaultPageImageDescriptor(ConverterPlugin.imageDescriptorFromPlugin(
			"icon/import_wiz.png"));
	}

	@Override
	protected void addUserInputPages() {
		addPage(new JMLWizardUserPage("Warning"));
	}

}

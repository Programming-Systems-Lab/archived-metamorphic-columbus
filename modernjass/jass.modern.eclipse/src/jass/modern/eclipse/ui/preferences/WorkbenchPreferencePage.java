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
package jass.modern.eclipse.ui.preferences;

import jass.modern.core.util.Configuration;
import jass.modern.core.util.Configuration.Issue;
import jass.modern.eclipse.ModernJassPlugin;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class WorkbenchPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private ComboFieldEditor fCfeEmptySpec;
	private ComboFieldEditor fCfeNonPure;
	private ComboFieldEditor fCfePartialSpec;
	private ComboFieldEditor fCfeOldOfNonePure;
	
	public WorkbenchPreferencePage() {
		setTitle("Contract Creation Options");
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(ModernJassPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void performApply() {
		fCfeNonPure.store();
		fCfeEmptySpec.store();
		fCfePartialSpec.store();
		fCfeOldOfNonePure.store();
		
		super.performApply();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		fCfeNonPure = createComboFieldEditor(composite, Issue.NON_PURE_USE, "Refering to none-pure method");
		fCfeEmptySpec = createComboFieldEditor(composite, Issue.EMPTY_SPEC_CASE, "Empty method specification");
		fCfePartialSpec = createComboFieldEditor(composite, Issue.PARTIAL_SPEC_CASE, "Method specification is not complete");
		fCfeOldOfNonePure = createComboFieldEditor(composite, Issue.OLD_OF_NONE_PURE, "Using mutable reference types with @Old");
		
		return composite;
	}

	private ComboFieldEditor createComboFieldEditor(Composite parent, Issue issue, String label) {
		ComboFieldEditor tmp = new ComboFieldEditor(issue.getIdentifier(), label, comboOptions(), parent);
		tmp.fillIntoGrid(parent, 2);
		tmp.setPreferenceStore(getPreferenceStore());
		tmp.load();
		
		return tmp;
	}
	
	private String[][] comboOptions(){
		return new String[][] { 
			new String[] {"Ignore", Configuration.Type.IGNORE.name()}, 
			new String[] {"Warning",Configuration.Type.WARNING.name()},
			new String[] {"Error", 	Configuration.Type.ERROR.name()}
		};
	}
}

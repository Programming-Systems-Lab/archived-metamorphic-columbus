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
package jass.modern.eclipse.ui.action;

import jass.modern.eclipse.ModernJassPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.LaunchAction;

public final class ContractLaunchAction extends LaunchAction {

	private ILaunchConfiguration fConfiguration;
	private String fMode;
	
	public ContractLaunchAction(ILaunchConfiguration configuration,
			String mode) {
		super(configuration, mode);
		fConfiguration = configuration;
		fMode = mode;
	}

	@Override
	public void run() {
		try {
			ILaunchConfigurationWorkingCopy workingCopy = fConfiguration.getWorkingCopy();
			ModernJassPlugin.enableContractChecks(workingCopy);
			DebugUITools.launch(workingCopy, fMode);
			
		} catch (CoreException e) {
			ModernJassPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, 
					ModernJassPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}
	
}

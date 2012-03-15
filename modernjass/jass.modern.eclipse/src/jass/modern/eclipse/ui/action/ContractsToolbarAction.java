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
package jass.modern.eclipse.ui.action;

import jass.modern.eclipse.ModernJassPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction;
import org.eclipse.debug.ui.actions.LaunchAction;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public abstract class ContractsToolbarAction extends AbstractLaunchToolbarAction {
	
	protected boolean fDebug;
	
	public ContractsToolbarAction(boolean debug) {
		super(debug ? IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP : 
			IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
		
		fDebug = debug;
	}
	
	@Override
	public void run(IAction action) {
		try {
			
			ILaunchConfigurationWorkingCopy launchConfig = getLastLaunch().getWorkingCopy();
			ModernJassPlugin.enableContractChecks(launchConfig);
			DebugUITools.launch(launchConfig, fDebug ? ILaunchManager.DEBUG_MODE : 
				ILaunchManager.RUN_MODE);
			
		} catch (CoreException e) {
			ModernJassPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, 
					ModernJassPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		Menu menu = super.getMenu(parent);
		menu = rewireMenu(parent, menu);
		
		return menu;
	}

	private Menu rewireMenu(Control parent, Menu menu) {
		
		Menu rewiredMenu = new Menu(parent);
		
		MenuItem[] items = menu.getItems();
		for (MenuItem item : items) {
			Object data = item.getData();
			
			if (data != null && data instanceof ActionContributionItem) {
				ActionContributionItem tmp = (ActionContributionItem) data;
				IAction action = tmp.getAction();
				
				if (action instanceof LaunchAction) {
					
					ILaunchConfiguration configuration = Accessor.getField(
							"fConfiguration", action, LaunchAction.class);
					String mode = Accessor.getField(
							"fMode", action, LaunchAction.class);
					IAction cAction = new ContractLaunchAction(configuration, mode);
					cAction.setText(action.getText());
					
					ActionContributionItem contrib = new ActionContributionItem(cAction);
					contrib.fill(rewiredMenu, -1);
				}
			}
		}
		
		return rewiredMenu;
	}
}

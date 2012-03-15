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
package jass.modern.eclipse;

import jass.modern.core.util.Configuration;
import jass.modern.core.util.Configuration.Attribute;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ModernJassPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "jass.modern.eclipse";

	// The shared instance
	private static ModernJassPlugin plugin;

	/**
	 * The arguments which are to added in order to
	 * enable contract checking.
	 * <ul>
	 * <li><code>-ea</code>
	 * <li><code>-javaagent:<i>modern.jass.core-VERSION.jar</i></code>
	 * </ul>
	 * @see #getModernJassLibraryEntry()
	 */
	public static final List<String> CONTRACT_ARGS = Arrays.asList(
			"-ea", 
			"-javaagent:" + getModernJassLibraryEntry().getPath().toOSString());

	private static final String VM_ARGS = "org.eclipse.jdt.launching.VM_ARGUMENTS";
	
	/**
	 * The constructor
	 */
	public ModernJassPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		IPreferenceStore preferences = getPreferenceStore();
		preferences.addPropertyChangeListener(new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				Configuration.setValue(event.getProperty(), event.getNewValue().toString());
			}
			
		});
		
		Collection<Attribute> defaultPreferences = Configuration.getDefaultConfiguration();
		for (Attribute attribute : defaultPreferences) {
			preferences.setDefault(
					attribute.getKey().getIdentifier(), 
					attribute.getValue().name());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ModernJassPlugin getDefault() {
		return plugin;
	}

	
	public static ImageDescriptor imageDescriptorFromPlugin(String imageFilePath) {
		return imageDescriptorFromPlugin(PLUGIN_ID, imageFilePath);
	}

	/**
	 * Returns the Modern Jass classpath entry.
	 * 
	 * @return
	 */
	public static IClasspathEntry getModernJassLibraryEntry() {
	
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		if(bundle == null)
			return null;
		
		URL local= null;
		try {
			local= FileLocator.toFileURL(bundle.getEntry("/"));
			
		} catch (IOException e) {
			return null;
		}
		
		String fullPath= new File(local.getPath()).getAbsolutePath();
		IPath path = Path.fromOSString(fullPath).append("jass.modern.core-20071125.jar");
		IPath src = Path.fromOSString(fullPath).append("jass.modern.core-20071125-src.jar");
		
		return JavaCore.newLibraryEntry(path, src, null);
	}

	/**
	 * Enables contract checking for the passed launch configuration.
	 * Note that the launch config is a <i>working copy</i> only and
	 * changed must be saved manually in order to stored them.
	 * 
	 * @param launchConfig
	 * @throws CoreException
	 */
	public static void enableContractChecks(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		String vmArgs = launchConfig.getAttribute(VM_ARGS, "");
		
		for (String argument : CONTRACT_ARGS) {
			if(! vmArgs.contains(argument)) {
				vmArgs += " " + argument;
			}
		}
		
		launchConfig.setAttribute(VM_ARGS, vmArgs);
	}

}

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
package jass.modern.eclipse.ui;

import jass.modern.eclipse.ModernJassPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class ContractInheritanceImageProvider implements IAnnotationImageProvider {

	final ImageDescriptor fImage = ModernJassPlugin.imageDescriptorFromPlugin("icons/dbc_ov-a.gif");
	
	public ContractInheritanceImageProvider() {
	
	}

	public Image getManagedImage(Annotation annotation) {
		return null;
	}

	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return fImage;
	}

	public String getImageDescriptorId(Annotation annotation) {
		return "dbc4j.eclipse.ContractInheritance";
	}

}
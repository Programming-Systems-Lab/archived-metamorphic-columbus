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
package jass.modern.core.model;

import java.util.List;

public interface IExecutable extends IElement, IParameterized {
	
	List<String> getExceptions();
	
	void addException(String execption);
	
	void removeException(String exec);
	
	List<IVariable> getParameters();
	
	void addParameter(IVariable var);
	
	void removeParameter(IVariable var);
	
	String getReturnType();
	
	void setReturnType(String type);
	
	boolean isConstructor();
	
	void setConsutructor(boolean constructor);
}

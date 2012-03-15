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
package jass.modern.core.compile.creation;

import jass.modern.Invariant;
import jass.modern.core.model.IAnnotation;
import jass.modern.core.model.IType;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * An {@link IContractCreator} is creates Java code from a certain
 * kind of contract annotation. The only method of this interface is 
 * {@link #create(IAnnotation, IType, DiagnosticListener)} which gives 
 * you the parent type, the annotation, and a {@link DiagnosticListener}.
 * All type elements are fully resolved, so that they can be traversed by a
 * visitor.
 * <br/>
 * For instance, a {@link IContractCreator} might read the {@link Invariant}
 * annotation and creates the following contract code.
 * <table border="1">
 * <td>
 * <pre>
 * ...
 * &#064;Invar("fSize >= 0")
 * int fSize;
 * ...
 * </pre>
 * </td>
 * <td>
 * &nbsp;----is transformed to --->&nbsp;
 * </td>
 * <td>
 * <pre>
 * ...
 * int fSize;
 * 
 * private boolean fSize$invar(){ return fSize >= 0; }
 * ...
 * </pre>
 * </td>
 * </table>
 * In order to be called by the {@link ContractCreationController}, a 
 * {@link IContractCreator} needs to specifiy what annotations he is
 * interested at. This is done via the meta-annotation {@link ContractTypes}.
 * 
 * @author riejo
 */
public interface IContractCreator {

	/**
	 * A meta-annotation which is used to express
	 * that a {@link IContractCreator} works with
	 * the denoted (contract) annotations.
	 * <br />
	 * For instance, the FooInvariantContractCreator
	 * will be called only, when the {@link Invariant}-annotation 
	 * was found.
	 * <pre>
	 * &#064;ContractTypes( Invar.class)
	 * class FooInvariantContractCreator implements IContractCreator {
	 * 	//...
	 * }
	 * </pre>
	 * 
	 * @author riejo
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ContractTypes{
		
		Class<? extends Annotation>[] value();
	}
	
	/**
	 * Creation of the contract. This is a contract as Java
	 * source, not bytecode.
	 * 
	 * @param annotation The contract annotation
	 * @param parent The enclosing {@link IType} of the contract
	 * @param diagnostics Can be used to file a message/info/error
	 */
	public abstract void create(IAnnotation annotation, IType parent,
			DiagnosticListener<JavaFileObject> diagnostics);

}

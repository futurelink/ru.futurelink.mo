/*******************************************************************************
 * Copyright (c) 2013-2014 Pavlov Denis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pavlov Denis - initial API and implementation
 ******************************************************************************/

package ru.futurelink.mo.orm.dto.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pavlov
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AccessChecker {

	/**
	 * Access checker class.
	 * 
	 * @return
	 */
	Class<? extends IDTOAccessChecker> checker();	
	
	/**
	 * Access checker name.
	 * 
	 * @return
	 */
	String			name();
	
	/**
	 * Parameters for checker initialization. The array contains
	 * method names which return values used in initialization.
	 * 
	 * @return
	 */
	String[]		params();

}

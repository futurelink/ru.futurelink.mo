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

package ru.futurelink.mo.orm.iface;

/**
 * @author pavlov
 *
 */
public interface ICodeSupport {

	public Long getId();
	
	public ICommonObject getObject();
	public void setObject(ICommonObject object);

	public void setObjectClass(String className);
	public String getObjectClass();

}

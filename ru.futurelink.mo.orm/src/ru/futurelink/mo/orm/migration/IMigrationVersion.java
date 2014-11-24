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

package ru.futurelink.mo.orm.migration;

import ru.futurelink.mo.orm.iface.ICommonObject;

/**
 * @author pavlov
 *
 */
public interface IMigrationVersion 
	extends ICommonObject {

	/**
	 * Номер версии базы данных.
	 */
	public Integer getVersion();	
	public void setVersion(Integer version);
	
	/**
	 * Ревизия данной версии, из SVN или GIT.
	 */
	public String getRevision();
	public void setRevision(String revision);

}

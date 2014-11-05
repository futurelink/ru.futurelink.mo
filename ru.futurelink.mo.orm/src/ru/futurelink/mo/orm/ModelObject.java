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

package ru.futurelink.mo.orm;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import ru.futurelink.mo.orm.exceptions.SaveException;

@MappedSuperclass
public abstract class ModelObject implements Serializable {
	private static final long serialVersionUID = 1L;	

	abstract public String getId();
	abstract public void setId(String id);
	
	abstract public Boolean getDeleteFlag();
	abstract public void setDeleteFlag(Boolean deleteFlag);
	
	// Abstract method to save model
	abstract public Object save() throws SaveException;
	abstract public void saveCommit() throws SaveException;
}

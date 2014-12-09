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

import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface IModelObject {
	public		void	setId(String id);
	public 		String	getId();

	public 		Boolean getDeleteFlag();
	public 		void setDeleteFlag(Boolean deleteFlag);

	public Object save() throws SaveException;
	public void saveCommit() throws SaveException;
	
	public Accessors getAccessors(String fieldName) throws NoSuchFieldException;
}

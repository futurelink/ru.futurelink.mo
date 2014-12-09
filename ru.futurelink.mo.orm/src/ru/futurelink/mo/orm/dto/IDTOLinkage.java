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

package ru.futurelink.mo.orm.dto;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.IModelObject;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * Linkage interface for DTO.
 *
 * Assume there is one data object and we want to link it to another data
 * object. Also we want to have ability to unlink it. This linked data object may
 * already exist before link is created.
 *
 * So there are three objects in a chain: the object itself (parent), link object and
 * linked object.
 *
 * Adding a link means creating link object between parent object and linked object.
 * Removing a link doesn't delete linked object but removes a linkage.
 *
 * The example of usage is: user-shared subscription when any user can create linked
 * object and any other user may use it.
 *
 * @author pavlov
 *
 */
public interface IDTOLinkage {
	/**
	 * Set linked data object.
	 * 
	 * @param linkageItem
	 */
	public void setLinkedItem(IModelObject linkageItem);

	/**
	 * Get linked DTO on linked data object.
	 * 
	 * @return
	 */
	public EditorDTO getLinkedDTO();

	/**
	 * Activate linkage.
	 * 
	 * @throws DTOException
	 */
	public void activateLinkage() throws DTOException;

	/**
	 * Deactivate linkage.
	 * 
	 * @throws DTOException
	 */
	public void deactivateLinkage() throws DTOException;

	/**
	 * Get if linkage is active or inactive.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public Boolean getLinkageActive() throws DTOException;

	/**
	 * Get if the linked object was persisted.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public Boolean getLinkagePersisted() throws DTOException;

	/**
	 * Get linkage object creator.
	 * 
	 * @return
	 */
	public IUser getLinkageCreator();
}

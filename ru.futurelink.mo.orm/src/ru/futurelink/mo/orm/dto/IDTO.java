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

import java.util.ArrayList;
import java.util.HashMap;

import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * DTO is an in-memory data access layer between model object and controller.
 * This layer make it possible to collect data changes from controllers etc. without
 * immediately persisting it into database. Also changes may be reverted and
 * ano DTO restored to initial state.
 *
 * DTO is reflective. It means it uses reflection API to access data model objects.
 *
 * @author pavlov
 *
 */
public interface IDTO {
	/**
     * Get data from model object. This method must be supplied with
     * getter and setter method names for model object to work properly.
     *
     * If data was changed in DTO then this data is the returned data.
	 *
	 * @param fieldGetterName getter method name in model object
	 * @param fieldSetterName setter method name in model object
	 * @return
	 * @throws DTOException
	 */	
	public Object getDataField(String fieldName, String fieldGetterName, 
			String fieldSetterName) throws DTOException;

    /**
     * Get data from model object. This method must be supplied with
     * getter and setter method names for model object to work properly.
     * Checks access ability on access checker provided by setAccessChecker
     * if needed.
     *
     * If data was changed in DTO then this data is the returned data.
     *
     * @param fieldName
     * @param fieldGetterName getter method name in model object
     * @param fieldSetterName setter method name in model object
     * @param checkAccess access check flag
     * @return
     * @throws DTOException
     */
	public Object getDataField(String fieldName, String fieldGetterName, 
			String fieldSetterName, boolean checkAccess) throws DTOException;

	/**
	 * Set object model data field in DTO. New value is to be stored in
     * DTO's changes buffer, but will not be persisted until save() is called.
	 * 
	 * @param fieldGetterName getter method name in model object
	 * @param fieldSetterName setter method name in model object
	 * @param value
	 * @throws DTOException
	 */
	public void setDataField(String fieldName, String fieldGetterName, 
			String fieldSetterName, Object value) throws DTOException;

	/**
	 * Clear changes from DTO, restore initial database state of object.
	 * 
	 * @return
	 */
	public void clearChangesBuffer();

	/**
	 * Get the state of DTO's chages buffer.
	 * 
	 * @return
	 */
	public HashMap<String, Object[]> getChangesBuffer();

    /**
     * Get the list of data field names from DTO's changes buffer.
     *
     * @return
     * @throws DTOException
     */
    public ArrayList<String> getChangedData() throws DTOException;

	/**
	 * Clear DTO.
	 */
	public void clear();
	
	/**
	 * Persist DTO changes into model object.
	 * 
	 * @throws DTOException
	 * @throws SaveException
	 */
	public void save() throws DTOException, SaveException;
	
	/**
	 * Get model object ID. In common case the ID is the database ID of
     * record (object).
	 * 
	 * @return
	 * @throws DTOException
	 */
	public String getId() throws DTOException;
	
	/**
	 * Get data delete flag.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public boolean getDeleteFlag() throws DTOException;
	
	/**
	 * Set data delete flag.
	 * 
	 * @param deleteFlag
	 * @throws DTOException
	 */
	public void setDeleteFlag(boolean deleteFlag) throws DTOException;	
	
	/**
	 * Get data creator user.
     *
	 * @return
	 */
	public IUser getCreator();

	/**
	 * Add access check agent to DTO.
	 * 
	 * @param checker
	 */
	public void addAccessChecker(IDTOAccessChecker checker);
	
	/**
	 * Get access check agent from DTO.
	 * 
	 * @return
	 */
	public IDTOAccessChecker getAccessChecker();	
}

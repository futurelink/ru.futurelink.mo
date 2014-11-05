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

import java.util.Date;

import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * Результирующий класс модели для отображения списка истории
 * элемента данных.
 * 
 * @author pavlov
 *
 */
final public class HistoryResult extends ModelObject {

	private static final long serialVersionUID = 1L;

	public HistoryResult(String id, Date date, String operation, String objectId) {
		setId(id);
		setDate(date);
		setOperation(operation);
		setObjectId(objectId);
	}

	private Date mDate;
	public void setDate(Date date) { mDate = date; }
	public Date getDate() { return mDate; }

	private String mOperation;
	public void setOperation(String operation) { mOperation = operation; }
	public String getOperation() { return mOperation; }

	/**
	 * ID объекта
	 */
	private		String mId;	
	public 		String getId() {	return mId;	}
	public		void setId(String id) { mId = id; }
	
	private		String mObjectId;
	public		String getObjectId() { return mObjectId; }
	public		void setObjectId(String objectId) { mObjectId = objectId; }
	
	public		Boolean	getDeleteFlag() { return false; }

	@Override
	public void setDeleteFlag(Boolean deleteFlag) {}

	@Override
	public Object save() throws SaveException {
		throw new SaveException("Saving history object is not allowed!", null);
	}

	@Override
	public void saveCommit() throws SaveException {
		throw new SaveException("Saving history object is not allowed!", null);
	}
}

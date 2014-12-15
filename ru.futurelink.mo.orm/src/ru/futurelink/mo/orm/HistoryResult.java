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

import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * Результирующий класс модели для отображения списка истории
 * элемента данных.
 * 
 * @author pavlov
 *
 */
final public class HistoryResult extends ModelObject {

	private static final long serialVersionUID = 1L;

	public HistoryResult(String id, Date date, String operation, String objectId, IUser author) {
		setId(id);
		setDate(date);
		setOperation(operation);
		setObjectId(objectId);
		setAuthor(author);
	}

	@Accessors(getter = "getDate", setter = "setDate")
	private Date date;
	public void setDate(Date date) { this.date = date; }
	public Date getDate() { return date; }

	@Accessors(getter = "getOperation", setter = "setOperation")
	private String operation;
	public void setOperation(String operation) { this.operation = operation; }
	public String getOperation() { return operation; }

	/**
	 * ID объекта
	 */
	@Accessors(getter = "getId", setter = "setId")
	private		String id;	
	public 		String getId() {	return id;	}
	public		void setId(String id) { this.id = id; }
	
	@Accessors(getter = "getObjectId", setter = "setObjectId")
	private		String objectId;
	public		String getObjectId() { return objectId; }
	public		void setObjectId(String objectId) { this.objectId = objectId; }
	
	@Accessors(getter = "getDeleteFlag", setter = "setDeleteFlag")
	private		Boolean deleteFlag;
	public		Boolean	getDeleteFlag() { return false; }

	@Accessors(getter = "getAuthor", setter = "setAuthor")
	private		IUser	author;
	public		void	setAuthor(IUser author) { this.author = author; }
	public		IUser	getAuthor() { return author; }

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

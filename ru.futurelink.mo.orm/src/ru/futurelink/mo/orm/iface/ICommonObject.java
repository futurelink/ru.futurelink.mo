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

import java.util.Date;

import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.IUser;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

/**
 * @author pavlov
 *
 */
public interface ICommonObject extends IModelObject {

	public 		ICodeSupport getCode();

	public 		IUser	getCreator();
	public		void	setCreator(IUser creator);  

	public 		IUser	getAuthor();
	public		void	setAuthor(IUser author);  

	public void setPersistentManagerSession(IPersistentManagerSession pm);
	public IPersistentManagerSession getPersistenceManagerSession();

	public	ICommonObject 	getUnmodifiedObject();
	public	void 			setUnmodifiedObject(ICommonObject object);

	public Object onBeforeSave(int saveFlag) throws SaveException;
	public Object onAfterSave(int saveFlag) throws SaveException;

	public void setModifyDate(Date time);
	public Date getModifyDate();

	public void setCreateDate(Date time);
	public Date getCreateDate();
	
	public	void setWorkLog(IWorkLog w);
	public	IWorkLog getWorklog();
	
	public boolean getWorkLogEnabled();
	public IWorkLog createWorkLog();
	
	public void forceUpdateField(String field, ICommonObject dataItem);

	public 		void delete();
	public		void recover();

}


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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;

/**
 * @author pavlov
 *
 */
public class PersistentManagerSession {

	private PersistentManager		mPersistent;
	private User					mUser;
	private User					mAccessUser;
	private EntityTransaction		transaction;

	/**
	 * 
	 */
	public PersistentManagerSession(PersistentManager persistent) {
		mPersistent = persistent;
	}

	public PersistentManager getPersistentManager() { return mPersistent; }
	
	/**
	 * Пользователь от имени которого совершаются действия
	 * персистент-менеджера.
	 */
	public void setUser(User user) { mUser = user; }
	public User getUser() { return mUser; } 

	/**
	 * Пользователь, которому принадлжеат данные.
	 */
	public void setAccessUser(User user) { mAccessUser = user; }
	public User getAccessUser() {
		if (mAccessUser == null) 
			return mUser; 
		else
			return mAccessUser; 
	} 
	
	@SuppressWarnings("unchecked")
	public <T extends CommonObject> T open(Class<T> cls, String id) throws OpenException {
		CommonObject obj = getPersistentManager().open(cls, id);
		obj.setPersistentManagerSession(this);
		return (T) obj;
		
	}

	public EntityManager getEm() {
		return getPersistentManager().getEm();
	}
	
	public EntityManager getOldEm() {
		return getPersistentManager().getOldEm();
	}
	
	public boolean transactionIsOpened() {
		return (transaction != null);
	}

	public void transactionBegin() {
		if (transaction == null) {
			transaction = getEm().getTransaction();
			transaction.begin();
		}
	}
	
	public void transactionCommit() {
		if (transaction != null) {
			transaction.commit();
			transaction = null;
		}
	}
	
	public void transactionRollback() {
		if (transaction != null) {
			transaction.rollback();
			transaction = null;
		}
	}
	
	public Object save(CommonObject object) throws SaveException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}

		return getPersistentManager().save(object, this);
	}

	public Object saveWithHistory(HistoryObject object, String oldId) throws SaveException, OpenException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}

		return getPersistentManager().saveWithHistory(object, oldId, this);
	}
}

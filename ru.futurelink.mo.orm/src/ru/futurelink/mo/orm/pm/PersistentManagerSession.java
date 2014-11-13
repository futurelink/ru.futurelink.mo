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

package ru.futurelink.mo.orm.pm;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.HistoryObject;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public class PersistentManagerSession 
	implements IPersistentManagerSession {

	private PersistentManager		mPersistent;
	private EntityTransaction		transaction;
	
	private EntityManager			entityManager;
	private EntityManager			oldEntityManager;

	public PersistentManagerSession(PersistentManager persistent) {
		mPersistent = persistent;
	}

	public PersistentManager getPersistentManager() { return mPersistent; }
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends CommonObject> T open(Class<T> cls, String id) throws OpenException {
		CommonObject obj = getPersistentManager().open(cls, id, this);
		obj.setPersistentManagerSession(this);
		return (T) obj;
		
	}

	@Override
	public EntityManager getEm() {
		if (entityManager == null) {
			entityManager = getPersistentManager().createEntityManager();
		}

		if (entityManager == null) {
			throw new RuntimeException("Нет доступного EntityManager, вероятно фабрику создать не удалось!");	
		} else
			return entityManager;
	}
	
	@Override
	public EntityManager getOldEm() {
		if (oldEntityManager == null) {
			oldEntityManager = getPersistentManager().createEntityManager();
		}

		if (oldEntityManager == null) {
			throw new RuntimeException("Нет доступного EntityManager, вероятно фабрику создать не удалось!");	
		} else
			return oldEntityManager;
	}
	
	@Override
	public boolean transactionIsOpened() {
		return (transaction != null);
	}

	@Override
	public void transactionBegin() {
		if (transaction == null) {
			transaction = getEm().getTransaction();
			transaction.begin();
		}
	}

	@Override
	public void transactionCommit() {
		if (transaction != null) {
			transaction.commit();
			transaction = null;
		}
	}

	@Override
	public void transactionRollback() {
		if (transaction != null) {
			transaction.rollback();
			transaction = null;
		}
	}

	@Override
	public Object save(CommonObject object) throws SaveException {
		return getPersistentManager().save(object, this);
	}

	@Override
	public Object saveWithHistory(HistoryObject object, String oldId) 
			throws SaveException, OpenException {
		return getPersistentManager().saveWithHistory(object, oldId, this);
	}

	@Override
	public Logger logger() {
		return getPersistentManager().logger();
	}
}

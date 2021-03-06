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

package ru.futurelink.mo.orm.entities.security;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;

import ru.futurelink.mo.orm.exceptions.LockException;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

/**
 * Persistent objects lock table stored in data base table.
 * 
 * @author pavlov_d
 *
 */
@Entity(name = "UserLock")
@Table(name = "USER_LOCKS")
public class UserLock implements Serializable { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Object ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private		Long id;	
	public 		Long getId() {	return id;	}
	public		void setId(Long id) { this.id = id; }	
	
	/**
	 * Lock set time
	 */
	@Column(name = "lockBegin")
	@Temporal(TemporalType.TIMESTAMP)
	private Date 	mLockBegin; 
	
	/**
	 * The user who set lock
	 */
	@JoinColumn(name = "lockUser", referencedColumnName="id")
	private User 	mLockUser;
	public User		getLockUser() { return mLockUser; }
	
	/**
	 * What's the class of object has been locked
	 */
	@Column(name = "objectClassName")
	private String 	mObjectClassName;
	public String	getObjectClassName() { return mObjectClassName; }
	
	/**
	 * What is an object ID?
	 */
	@Column(name = "objectId")
	private String 	mObjectId;
	public String	getObjectId() { return mObjectId; }
	
	/**
	 * Gets the lock information.
	 * 
	 * @param objClassName
	 * @param objId
	 * @return
	 */
	private static UserLock getLockInfo(IPersistentManagerSession pms, String objClassName, String objId) {
		
		// Получаем данные о том, залочен ли объект
		Query q = pms.getEm().createQuery("select lock from UserLock lock "
				+ "where lock.mObjectClassName = :class and lock.mObjectId = :id");
		q.setParameter("class", objClassName);
		q.setParameter("id", objId);
		UserLock lock = null;
		try {
			lock = (UserLock) q.getSingleResult();
		} catch(NoResultException nr) {
			lock = null;
		}

		return lock;		
	}
	
	/**
	 * Sets the lock on object with ID by user.
	 * 
	 * @param objClassName
	 * @param objId
	 * @param user
	 * @return
	 */
	private static UserLock setLock(IPersistentManagerSession pms, String objClassName, String objId, User user) {
		UserLock lock = new UserLock();
		lock.mLockBegin = Calendar.getInstance().getTime();
		lock.mLockUser = user;
		lock.mObjectClassName = objClassName;
		lock.mObjectId = objId;
		
		pms.getEm().getTransaction().begin();
		pms.getEm().persist(lock);
		pms.getEm().getTransaction().commit();
	
		return lock;
	}
	
	/**
	 * Sets the lock on object by ID by anonymous user.
	 * 
	 * @param objClassName
	 * @param objId
	 * @return - ID блокировки
	 */
	public static UserLock acquireLock(IPersistentManagerSession pms, String objClassName, String objId) throws LockException {		
		return acquireLock(pms, objClassName, objId, null);
	}
	
	/**
	 * Request for lock on object by ID.
	 * 
	 * @param objClassName
	 * @param objId
	 * @param lockUser
	 * @throws LockException 
	 * @return - lock ID
	 */
	public static UserLock acquireLock(IPersistentManagerSession pms, String objClassName, String objId, User lockUser) throws LockException {
		UserLock lock = getLockInfo(pms, objClassName, objId);
		
		// Если залочен - вываливаем эксепшн
		if (lock != null) {
			LockException le = new LockException();
			le.lockExceptionType = 0;
			le.lockSetTime = lock.mLockBegin;
			if (lock.getLockUser() != null) {
				le.lockUserName = lock.getLockUser().getLogin();
			} else {
				le.lockUserName = "Система";
			}
			throw le;
		}		

		return setLock(pms, objClassName, objId, lockUser);
	}
	
	/**
	 * Unlock an object by ID.
	 * 
	 * @param objClassName
	 * @param objId
	 * @throws LockException
	 */
	public static void releaseLock(IPersistentManagerSession pms, String objClassName, String objId) throws LockException {
		UserLock lock = getLockInfo(pms, objClassName, objId);
		
		if (lock == null) {
			LockException le = new LockException();
			le.lockExceptionType = 1;
			le.lockSetTime = null;
			le.lockUserName = "Система";
			throw le;
		}

		// Отменяем блокировку
		lock = pms.getEm().find(UserLock.class, lock.getId());
		pms.getEm().getTransaction().begin();
		pms.getEm().remove(lock);
		pms.getEm().getTransaction().commit();
	}
	
	/**
	 * Clear all locks.
	 * 
	 * @param pm
	 */
	public static void clearLocks(IPersistentManagerSession pms) {
		clearLocks(pms, null);
	}
	
	/**
	 * Clear all user locks.
	 * 
	 * @param pm
	 * @param lockUser
	 */
	public static void clearLocks(IPersistentManagerSession pms, User lockUser) {	
		Query q;
		if (lockUser != null) {
			q = pms.getEm().createQuery("delete from UserLock u where mLockUser = :user");
			q.setParameter("user", lockUser);
		} else {
			q = pms.getEm().createQuery("delete from UserLock u");			
		}
		pms.getEm().getTransaction().begin();
		q.executeUpdate();
		pms.getEm().getTransaction().commit();
	}
	
}

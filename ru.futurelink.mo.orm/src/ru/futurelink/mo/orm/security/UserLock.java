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

package ru.futurelink.mo.orm.security;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

import ru.futurelink.mo.orm.PersistentManager;
import ru.futurelink.mo.orm.exceptions.LockException;

/**
 * Таблица блокировок элементов данных
 * пользователями.
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
	 * ID объекта
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private		Long mId;	
	public 		Long getId() {	return mId;	}
	public		void setId(Long id) { mId = id; }	
	
	/**
	 * Когда установлена блокировка
	 */
	@Column(name = "lockBegin")
	@Temporal(TemporalType.TIMESTAMP)
	private Date 	mLockBegin; 
	
	/**
	 * Кем установлена блокировка.
	 */
	@JoinColumn(name = "lockUser", referencedColumnName="id")
	private User 	mLockUser;
	public User		getLockUser() { return mLockUser; }
	
	/**
	 * На какой класс?
	 */
	@Column(name = "objectClassName")
	private String 	mObjectClassName;
	public String	getObjectClassName() { return mObjectClassName; }
	
	/**
	 * На какой элемент?
	 */
	@Column(name = "objectId")
	private String 	mObjectId;
	public String	getObjectId() { return mObjectId; }
	
	/**
	 * Выбрать информацию по блокировкам элементов данных.
	 * @param objClassName
	 * @param objId
	 * @return
	 */
	private static UserLock getLockInfo(PersistentManager pm, String objClassName, String objId) {
		
		// Получаем данные о том, залочен ли объект
		Query q = pm.getEm().createQuery("select lock from UserLock lock where lock.mObjectClassName = :class and lock.mObjectId = :id");
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
	 * Проставить блокировку в базе.
	 * @param objClassName
	 * @param objId
	 * @param user
	 * @return
	 */
	private static UserLock setLock(PersistentManager pm, String objClassName, String objId, User user) {
		UserLock lock = new UserLock();
		lock.mLockBegin = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		lock.mLockUser = user;
		lock.mObjectClassName = objClassName;
		lock.mObjectId = objId;
		
		pm.getEm().getTransaction().begin();
		pm.getEm().persist(lock);
		pm.getEm().getTransaction().commit();
	
		return lock;
	}
	
	/**
	 * Установить блокировку без пользователя.
	 * @param objClassName
	 * @param objId
	 * @return - ID блокировки
	 */
	public static UserLock acquireLock(PersistentManager pm, String objClassName, String objId) throws LockException {		
		return acquireLock(pm, objClassName, objId, null);
	}
	
	/**
	 * Установить блокировку пользователем.
	 * @param objClassName
	 * @param objId
	 * @param lockUser
	 * @throws LockException 
	 * @return - ID блокировки
	 */
	public static UserLock acquireLock(PersistentManager pm, String objClassName, String objId, User lockUser) throws LockException {
		UserLock lock = getLockInfo(pm, objClassName, objId);
		
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

		return setLock(pm, objClassName, objId, lockUser);
	}
	
	/**
	 * Отменить блокировку элемента пользователем.
	 * @param objClassName
	 * @param objId
	 * @throws LockException
	 */
	public static void releaseLock(PersistentManager pm, String objClassName, String objId) throws LockException {
		UserLock lock = getLockInfo(pm, objClassName, objId);
		
		if (lock == null) {
			LockException le = new LockException();
			le.lockExceptionType = 1;
			le.lockSetTime = null;
			le.lockUserName = "Система";
			throw le;
		}

		// Отменяем блокировку
		lock = pm.getEm().find(UserLock.class, lock.getId());
		pm.getEm().getTransaction().begin();
		pm.getEm().remove(lock);
		pm.getEm().getTransaction().commit();
	}
	
	/**
	 * Удалить все блокировки объектов вообще.
	 * 
	 * @param pm
	 */
	public static void clearLocks(PersistentManager pm) {
		clearLocks(pm, null);
	}
	
	/**
	 * Удалить все блокировки, сделанные пользователем.
	 * 
	 * @param pm
	 * @param lockUser
	 */
	public static void clearLocks(PersistentManager pm, User lockUser) {	
		Query q;
		if (lockUser != null) {
			q = pm.getEm().createQuery("delete from UserLock u where mLockUser = :user");
			q.setParameter("user", lockUser);
		} else {
			q = pm.getEm().createQuery("delete from UserLock u");			
		}
		pm.getEm().getTransaction().begin();
		q.executeUpdate();
		pm.getEm().getTransaction().commit();
	}
	
}

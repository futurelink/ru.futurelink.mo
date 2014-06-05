/**
 * 
 */
package ru.futurelink.mo.orm;

import javax.persistence.EntityManager;

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

	/**
	 * 
	 */
	public PersistentManagerSession(PersistentManager persistent) {
		mPersistent = persistent;
	}

	public PersistentManager getPersistent() { return mPersistent; }
	
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
		CommonObject obj = getPersistent().open(cls, id);
		obj.setPersistentManagerSession(this);
		return (T) obj;
		
	}

	public EntityManager getEm() {
		return getPersistent().getEm();
	}
	
	public EntityManager getOldEm() {
		return getPersistent().getOldEm();
	}
	
	public Object save(CommonObject object) throws SaveException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}

		return getPersistent().save(object, this);
	}

	public Object saveWithHistory(HistoryObject object, String oldId) throws SaveException, OpenException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager!", null);
		}

		return getPersistent().saveWithHistory(object, oldId, this);
	}
}

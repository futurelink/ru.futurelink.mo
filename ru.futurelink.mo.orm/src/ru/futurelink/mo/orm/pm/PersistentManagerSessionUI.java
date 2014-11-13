/**
 * 
 */
package ru.futurelink.mo.orm.pm;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.HistoryObject;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;

/**
 * @author pavlov
 *
 */
public class PersistentManagerSessionUI 
	extends PersistentManagerSession {

	private User					mUser;
	private User					mAccessUser;

	/**
	 * @param persistent
	 */
	public PersistentManagerSessionUI(PersistentManager persistent) {
		super(persistent);
	}

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

	@Override
	public Object save(CommonObject object) throws SaveException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}

		return super.save(object);
	}
	
	@Override
	public Object saveWithHistory(HistoryObject object, String oldId)
			throws SaveException, OpenException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}
		
		return super.saveWithHistory(object, oldId);
	}
}

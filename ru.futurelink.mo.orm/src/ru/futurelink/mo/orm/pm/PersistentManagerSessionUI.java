/**
 * 
 */
package ru.futurelink.mo.orm.pm;

import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IHistoryObject;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * @author pavlov
 *
 */
public class PersistentManagerSessionUI 
	extends PersistentManagerSession {

	private IUser					mUser;
	private IUser					mAccessUser;

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
	public void setUser(IUser user) { mUser = user; }
	public IUser getUser() { return mUser; } 

	/**
	 * Пользователь, которому принадлжеат данные.
	 */
	public void setAccessUser(IUser user) { mAccessUser = user; }
	public IUser getAccessUser() {
		if (mAccessUser == null) 
			return mUser; 
		else
			return mAccessUser; 
	}

	@Override
	public Object save(ICommonObject object) throws SaveException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}

		return super.save(object);
	}
	
	@Override
	public Object saveWithHistory(IHistoryObject object, String oldId)
			throws SaveException, OpenException {
		if (getUser() == null) {
			throw new SaveException("No user in persistent manager session!", null);
		}
		
		return super.saveWithHistory(object, oldId);
	}
}

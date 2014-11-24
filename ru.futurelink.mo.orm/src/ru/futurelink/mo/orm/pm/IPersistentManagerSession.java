/**
 * 
 */
package ru.futurelink.mo.orm.pm;

import javax.persistence.EntityManager;

import org.slf4j.Logger;

import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IHistoryObject;

/**
 * @author pavlov
 *
 */
public interface IPersistentManagerSession {
	public EntityManager getEm();
	public EntityManager getOldEm();
	
	public Logger logger();

	public boolean transactionIsOpened();
	public void transactionBegin();
	public void transactionCommit();
	public void transactionRollback();

	public <T extends ICommonObject> T open(Class<T> cls, String id) throws OpenException;
	public Object save(ICommonObject object) throws SaveException;
	public Object saveWithHistory(IHistoryObject object, String oldId) throws SaveException, OpenException;	
}

/**
 * 
 */
package ru.futurelink.mo.orm;

import javax.persistence.EntityManager;

import org.slf4j.Logger;

import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;

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

	public <T extends CommonObject> T open(Class<T> cls, String id) throws OpenException;
	public Object save(CommonObject object) throws SaveException;
	public Object saveWithHistory(HistoryObject object, String oldId) throws SaveException, OpenException;	
}

/**
 * 
 */
package ru.futurelink.mo.orm.migration;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.orm.PersistentManager;

/**
 * Процедура миграции данных, которая должна быть выполнена после того, как
 * обновятся схемы данных JPA.
 * 
 * Данные процедуры должны выполнить, если это необходимо, реструктуризацию данных,
 * перенос между таблицами. Может быть также удаление старых данных.
 * 
 * Миграцию не нужно выполнять на машине разработки, однако при развертывании новой
 * версии базы на рабочей или тестовой системе - нужно выполнить. Предполагается, что
 * машина разработки и так имеет нужную версию базы данных.
 * 
 * @author pavlov
 *
 */
public abstract class Migration {
	private		PersistentManager 	mPersistent;
	private		int					mVersionFrom;
	private		int					mVersionTo;
	private		Logger				mLogger;
	
	public Migration(PersistentManager pm, int versionFrom, int versionTo) {
		mPersistent = pm;
		mVersionFrom = versionFrom;
		mVersionTo = versionTo;
		
		mLogger = LoggerFactory.getLogger(getClass());
	}
	
	protected PersistentManager getPersistent() {
		return mPersistent;
	}
	
	protected int getVersionFrom() { return mVersionFrom; }
	protected int getVersionTo() { return mVersionTo; }

	protected Logger logger() { return mLogger; }
	
	/**
	 * Сохранить версию базы данных в бвзе данных. 
	 */
	private void storeVersion(String revision) {
		logger().info("Сохранение новой версии базы данных...");
		
		MigrationVersion version = new MigrationVersion();
		version.setVersion(getVersionTo());
		version.setRevision(revision);
		
		getPersistent().getEm().getTransaction().begin();
		getPersistent().getEm().persist(version);
		getPersistent().getEm().getTransaction().commit();
	}
	
	public abstract void doBeforeProcess() throws Exception;
	public abstract void doProcess() throws Exception;
	public abstract void doAfterProcess() throws Exception;
	
	/**
	 * Выполнить миграцию.
	 * 
	 * @throws Exception
	 */
	public void start(String revision) {
		// Проверить что текущая версия именно та, с которой надо проапдейтиться
		Integer maxVersion = 0;
		TypedQuery<Integer> maxVersionQuery = getPersistent().getEm().createQuery(
				"select max(version.mVersion) maxVersion from MigrationVersion version", Integer.class);
		if (maxVersionQuery.getResultList().size() > 0) {
			if (maxVersionQuery.getResultList().get(0) != null)
				maxVersion = maxVersionQuery.getResultList().get(0);			
		}
		
		if (maxVersion < getVersionFrom()) {
			logger().error("Текущая версия {} не соотвествует версии {} необходимой для применения этого обновления.", maxVersion, getVersionFrom());
			throw new RuntimeException("Не соответствующая версия, применение обновления невозможно.");
		}

		logger().info("Обработка обновления с версии {} до {} в ревизии {}.", new Object[] { getVersionFrom(), getVersionTo(), revision });
		logger().info("Предварительная обработка для апдейта...");
		try {
			doBeforeProcess();
		
			logger().info("Основная операция апдейта...");
			doProcess();
		
			logger().info("Последующие после апдейта процедуры...");
			doAfterProcess();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		// Сохранить версию базы
		storeVersion(revision);
	}
}

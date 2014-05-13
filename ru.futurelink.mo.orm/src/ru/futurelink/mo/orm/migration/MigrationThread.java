/**
 * 
 */
package ru.futurelink.mo.orm.migration;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author pavlov
 *
 */
public class MigrationThread extends Thread {			
	
	public BundleContext ctx;

	public MigrationThread(BundleContext context) {
		ctx = context;
	}
	
	@Override
	public void run() {
		ServiceTracker<MigrationEngine, MigrationEngine> mServiceTracker = null;
		
		/*
		 * Получаем доступ к сервису логирования
		 */
		LogService mLogService = (LogService) ctx.getService(
				ctx.getServiceReference(LogService.class.getName())
			);
	
		mLogService.log(LogService.LOG_INFO, "Запускаем фоновую миграцию");

		try {
			mServiceTracker = new ServiceTracker<MigrationEngine, MigrationEngine>(
					ctx, 
					MigrationEngine.class.getName(), 
					null);
			mServiceTracker.open();

			MigrationEngine engine = (MigrationEngine) mServiceTracker.waitForService(5000);
			if (engine != null) {
				engine.start();
				mLogService.log(LogService.LOG_INFO, "Миграция завершена");
			}
		} catch (InterruptedException e) {
			String exString = "Ожидание сервиса миграций прервано.";			
			mLogService.log(LogService.LOG_WARNING, exString);
	    } catch (IllegalStateException e) {
			String exString = "Ошибка при обработке миграции, бандл остановлен при попытке запуска миграции.";
			mLogService.log(LogService.LOG_WARNING, exString);
		} finally {
			if (mServiceTracker != null)
				mServiceTracker.close();
		}
	}
};	


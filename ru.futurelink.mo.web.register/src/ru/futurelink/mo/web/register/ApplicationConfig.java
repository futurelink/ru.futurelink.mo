package ru.futurelink.mo.web.register;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфигурация приложения. Содержит информацию о точках входа,
 * и собственной регистрации в качестве сервиса OSGi.
 * 
 * Для того, чтобы перечитать конфигурацию и применить новый набор точек
 * входа нужно удалить регистрацию и зарегистировать его заново.
 * 
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class ApplicationConfig implements ApplicationConfiguration {

	private ServiceRegistration						mRegistration;
	private CopyOnWriteArrayList<EntryPointInfo>	mEntryPoints;
	private Logger									mLogger;

	public ApplicationConfig(BundleContext context) {
		mEntryPoints = new CopyOnWriteArrayList<EntryPointInfo>();
		mLogger = LoggerFactory.getLogger(getClass());
	}

	public void setRegistration(ServiceRegistration reg) {
		mRegistration = reg;
	}

	public ServiceRegistration getRegistration() {
		return mRegistration;
	}

	/**
	 * Добавить точку входа в приложение. Точка входа добавляется во
	 * внутренний реестр. Для ее активации требуется повторная регистрация
	 * сервиса ApplicationConfiguration.
	 * 
	 * @param title
	 * @param url
	 * @param favicon
	 * @param factory
	 */
	@Deprecated
	public synchronized void addEntryPoint(String title, String url, String favicon,
			EntryPointFactory factory) {		
		EntryPointInfo info  = new EntryPointInfo();
		info.title = title;
		info.url = url;
		info.favicon = favicon;
		info.factory = factory;
		
		// Удалим точку, чтобы неповторялись. 
		// Не может быть больше одной на одном URL.
		removeEntryPoint(url);		
		
		mEntryPoints.add(info);
	}

	public synchronized void addEntryPoint(String title, String url, InputStream favicon,
			EntryPointFactory factory) {		
		EntryPointInfo info  = new EntryPointInfo();
		info.title = title;
		info.url = url;
		info.faviconStream = favicon;
		info.factory = factory;
		
		removeEntryPoint(url);		
		
		mEntryPoints.add(info);
	}

	/**
	 * Удалить точку входа из реестра.
	 * @param url
	 */	
	public synchronized void removeEntryPoint(String url) {
		for (EntryPointInfo info : mEntryPoints) {
			if (info.url.equals(url)) mEntryPoints.remove(info);
		}
	};

	/**
	 * Приложение будет полностью переконфигурировано на основе
	 * данных о точках входа в прилоежние.
	 */
	public void configure(Application application) {
		application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
		try {
			application.addStyleSheet(RWT.DEFAULT_THEME_ID, "/main.css");
		} catch (Exception ex) {
			mLogger.warn("Нет таблицы стилей приложений main.css в classpath бандла");
			mLogger.warn("ru.futurelink.mo.web.register, желательно завести фрагмент, который будет экспортировать");
			mLogger.warn("специфичный для этого приложения стиль в CSS с таким именем.");
		}

		for (final EntryPointInfo info : mEntryPoints) {
			Map<String, String> properties = new HashMap<String, String>();
			
			/*
			 * Подгрузим фавикон
			 */
			if ((info.favicon != null) && (ApplicationConfig.class.getClassLoader().getResourceAsStream(info.favicon) != null)) {
				application.addResource(info.favicon, new ResourceLoader() {
					@Override
					public InputStream getResourceAsStream(String arg0) throws IOException {
						return ApplicationConfig.class.getClassLoader().getResourceAsStream(info.favicon);
					}
				});
				properties.put( WebClient.FAVICON, info.favicon );
			} else if (info.faviconStream != null) {

				// Если у фавикона нет имени - надо его сгенерить на основе данных,
				// чтобы, когда RWT будет генерить на основании него файлики - они не размножались
				if (info.favicon == null) {
					try {
						BufferedInputStream stream = new BufferedInputStream(info.faviconStream);
						stream.mark(65535);	// Mark for rewind limit is 64kb for favicon

						// Generate MD5 digest of favicon stream
						MessageDigest md = MessageDigest.getInstance("MD5");
						new DigestInputStream(stream, md);
						while (stream.read() != -1) {}
						byte[] digest = md.digest();
						StringBuffer md5 = new StringBuffer();
						for (int i = 0; i < digest.length; i++) {
			                md5.append(Integer.toHexString(0xFF & digest[i]));
			            }
						info.favicon = "favicon_md5_"+new String(md5);

						stream.reset();	// Rewind it

						application.addResource(info.favicon, new StreamResourceLoader(stream));
						properties.put( WebClient.FAVICON, info.favicon );
					} catch (IOException | NoSuchAlgorithmException e) {
						mLogger.warn("Невозможно загрузить favicon!", e);
					}
				}
			} else {
				mLogger.warn("Невозможно загрузить favicon, по всей видимости он не доступен из classpath бандла ru.futurelin.mo.web.register");
			}

			// Зарегистрируем точку входа
			properties.put( WebClient.PAGE_TITLE, info.title );
			application.addEntryPoint(info.url, 
					info.factory, 
					properties);
		}
	}  

	private class EntryPointInfo {
		String title;
		String url;
		String favicon;
		InputStream faviconStream;
		EntryPointFactory factory;
	}
	
	class StreamResourceLoader implements ResourceLoader {
		private InputStream mStream;
		
		public StreamResourceLoader(InputStream stream) {
			mStream = stream;
		}
		
		@Override
		public InputStream getResourceAsStream(String arg0) throws IOException {
			return mStream;
		}
		
	}
	
}

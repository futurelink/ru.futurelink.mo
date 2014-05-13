package ru.futurelink.mo.web.composites;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Окно композита для всех целей. Класс используется в приложении как база для любых кастомных
 * композитов, контролов и прочего. Для создания своих контролов лучше использовать его, так как
 * этот класс имеет доступ к сессии приложения, к механизму локализации и прочим радостям.
 * 
 * Также CommonComposite получает набор параметров CompositeParams, которые могут быть 
 * использованы после создания. Параметры нужны для передачи состояния, вида композита из
 * управляющего им контроллера или от родительского контроллера, по отношению к которому
 * контроллер композита является субконтроллером. 
 * 
 * @author pavlov
 *
 */
public class CommonComposite extends Composite {

	private static final long serialVersionUID = 1L;

	private ApplicationSession mSession;
	private ResourceBundle		mStrings;
	private ResourceBundle		mLocalStrings;
	private CommonDialog		mOwnerDialog;	// Диалоговое окно - если открыто в диалоге
	private CompositeParams		mParams;

	private CommonControllerListener	mListener;
	private Logger				mLogger;

	public CommonComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(parent, style);
		
		setData(RWT.CUSTOM_VARIANT, "commonComposite");

		mLogger = LoggerFactory.getLogger(getClass());

		mSession = session;
		mParams	 = params;	
		mStrings = ResourceBundle.getBundle("locale/main", getLocale(), new UTF8Control());

		try {
			mLocalStrings = ResourceBundle.getBundle("locale/main", getLocale(), getClass().getClassLoader(), new UTF8Control());
		} catch(Exception e) {
			mLogger.warn("Нет строк locale/main для выбранной локали {} в пути класса", getLocale().getLanguage());
		}
	}

	protected CommonComposite(Composite parent) { 
		super(parent, SWT.NONE);
		
		setData(RWT.CUSTOM_VARIANT, "commonComposite");
	}

	public Logger logger() { return mLogger; }
	
	/**
	 * Метод инициализации композита. Вызывает непосредственно создание элементов
	 * интерфейса, привязку обработчиков и прочие важные штуки.
	 * 
	 * @throws InitException 
	 */
	public void init() throws InitException {}
	
	/**
	 * Метод для удобства. Возвращает локаль установленную в сессии пользователя.
	 * 
	 * @return объект локали типа Locale
	 */
	final public Locale getLocale() {
		if (mSession != null) {
			return mSession.getLocale();
		}
		return null;
	}

	/**
	 * Получить объект сессии пользователя.
	 * 
	 * @return объект сессии пользователя ApplicationSession.
	 */
	final public ApplicationSession getSession() {
		return mSession;
	}
	
	/**
	 * <p>Получить ResourceBundle для локализации строк в композите. Использование ResourceBundle
	 * возможно для локализации приложения. Загружается ресурс из файла 
	 * resources/locale/main_xx.properties. В котором описаны все строки приложения.</p>
	 * 
	 * <p>Работа с возвращасемым объектом происходит также как и любым объектом ResourceBundle.</p>
	 * 
	 * @return загруженный для сессии ResourceBundle.
	 */
	@Deprecated
	final public ResourceBundle getStrings() {
		return mStrings;
	}

	final public String getLocaleString(String stringName) {
		if (mLocalStrings != null) {
			try {
				return mLocalStrings.getString(stringName);
			} catch (MissingResourceException e) {
				try {
					return mStrings.getString(stringName);
				} catch (MissingResourceException e2) {
					return stringName;
				}
			}
		} else {
			return mStrings.getString(stringName);
		}
	}
	
	final public String getLocaleNumeric(Integer value, String single, String multiple) {
		if (value > 1) {
			return String.format(getLocaleString(multiple), value);
		} else {
			return String.format(getLocaleString(single), value);
		}
	}
	
	final public void setStrings(ResourceBundle bundle) {
		mStrings = bundle;
	}
	
	/**
	 * <P>Добавить объект обработчика контроллера к композиту. Обработчик необъодим любому композиту
	 * для общения с контроллером этого композита.</p>
	 * 
	 * <p>Для того, чтобы создать обработчик контроллера композита необходимо переопределить
	 * интерфейс CommonControllerListener, или любой подходящий унаслеованный от него и реализоать
	 * в коде контроллера.</p>
	 * 
	 * <p>Метод лучше всего вызывать в doAfterCreateComposite. А реализацию интерфеса обработчика
	 * делать в переопределенном createControllerListener.</p>
	 * 
	 * @param listener
	 */
	final public void addControllerListener(CommonControllerListener listener) {
		mListener = listener;
	}
	
	/**
	 * Получить привязанный обработчик контроллера привязанный к композиту методом
	 * addControllerListener.
	 * 
	 * @return объект CommonControllerListener или любой от него унаследованный
	 */
	public CommonControllerListener getControllerListener() {
		if (mListener == null) {
			if (CommonComposite.class.isInstance(getParent())) {
				return ((CommonComposite)getParent()).getControllerListener();
			} else {
				return null;
			}
		}
		return mListener;
	}
	
	/**
	 * <p>Метод устанавлиает диалога-владельца композита. Нехорошо использовать эту функцию
	 * в своем коде, она используется только в CommonDialog.</p>
	 * 
	 * <p><i>Метод не использовать во внешнем коде!</i></p>
	 * 
	 * @param dialog - диалог, который надо сделать владельцем композита.
	 */
	public final void setOwnerDialog(CommonDialog dialog) {
		mOwnerDialog = dialog;
	}
	
	/**
	 * <p>Вывести результат в диалог владелец композита. Диалог владелец - это такой диалог,
	 * к которому выполнен attachComposite этого композита.</p>
	 * 
	 * <p>Используется для того, чтобы из композита вернуть диалогу результат его работы. Например,
	 * диалоговое окно открывается модально, результат работы из композита передается в диалоговое
	 * окно, чтобы потом забрать его в том контроллере, из которого производился вызов этого
	 * модального окна.</p> 
	 * 
	 * @param result
	 */
	final protected void setOwnerDialogResult(Object result) {
		if (mOwnerDialog != null)
			mOwnerDialog.setResult(result);
	}
	
	/**
	 * <p>Получить параметр переданный композиту при создании. Как правило параметры передаются
	 * из контроллера при создании композита в переопределенном методе createComposite(...).</p>
	 * 
	 * <p>У композита нет доступа к структуре params(), как у контроллера, но есть возможность
	 * получить один параметр по имени.</p>
	 * 
	 * @param paramName название параметра
	 * @return значение параметра по имени
	 */
	protected Object getParam(String paramName) {
		if (mParams != null) 
			return mParams.get(paramName);
		return null;
	}
	
	public class UTF8Control extends Control {
	    public ResourceBundle newBundle
	        (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
	            throws IllegalAccessException, InstantiationException, IOException
	    {
	        // The below is a copy of the default implementation.
	        String bundleName = toBundleName(baseName, locale);
	        String resourceName = toResourceName(bundleName, "properties");
	        ResourceBundle bundle = null;
	        InputStream stream = null;
	        if (reload) {
	            URL url = loader.getResource(resourceName);
	            if (url != null) {
	                URLConnection connection = url.openConnection();
	                if (connection != null) {
	                    connection.setUseCaches(false);
	                    stream = connection.getInputStream();
	                }
	            }
	        } else {
	            stream = loader.getResourceAsStream(resourceName);
	        }
	        if (stream != null) {
	            try {
	                // Only this line is changed to make it to read properties files as UTF-8.
	                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
	            } finally {
	                stream.close();
	            }
	        }
	        return bundle;
	    }
	}
}

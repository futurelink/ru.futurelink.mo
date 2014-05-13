package ru.futurelink.mo.web.controller;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.iface.IBundleDecorator;
import ru.futurelink.mo.web.controller.iface.IController;
import ru.futurelink.mo.web.controller.iface.IMailSender;
import ru.futurelink.mo.web.controller.iface.ISessionDecorator;
import ru.futurelink.mo.web.exceptions.InitException;
import ru.futurelink.mo.web.register.UseCaseRegister;

public abstract class CommonController 
		implements IController, IMailSender, ISessionDecorator, IBundleDecorator 
{
		protected BundleContext						mBundleContext;

		protected Class<? extends CommonObject>		mDataClass;
		protected ApplicationSession					mSession;
		protected List<IController>					mSubControllers;
		protected IController							mThisController;
		
		protected boolean 							mInitialized;
		private CommonControllerListener 				mListener;
		private Logger									mLogger;

		//@SuppressWarnings("rawtypes")
		//private	 ServiceRegistration					mEventHandlerRegistration;
		
		public CommonController(ApplicationSession session, Class<? extends CommonObject> dataClass) {
			mSession = session;
			mDataClass = dataClass;
			mThisController = this;
			mSubControllers = new ArrayList<IController>();
			
			mLogger = LoggerFactory.getLogger(getClass());
		}

		/**
		 * <p>Инициализация контроллера. Метод инициализации необходимо вызывать всякий раз
		 * перед началом работы с контроллером, в некоторых случаях - сразу за созданием
		 * объекта этого контроллера.</p>
		 * 
		 * <p>Введение дополнительной процедуры инициализации обусловлено необходимость
		 * гибко управлять процессом создания контроллеров и подконтрольных объектов.</p>
		 * 
		 * <p>Метод init() выполнят процедуры doBeforeInit, doAfterInit, однако между ними
		 * осуществляет инициализацию всеж субконтроллеров в порядке их добавления
		 * методом addSubController(...).</p> 
		 * @throws InitException 
		 */
		@Override
		public synchronized void init() throws InitException {
			logger().debug("Инициализация контроллера");
			if (mBundleContext != null) {
				logger().debug("Мой контекст бандала: "+mBundleContext.toString());
				//registerEventHandler();
			} else 
				logger().info("Нет контекста бандла, запущен без взаимодействия OSGI.");
			doBeforeInit();
			for (IController sub : mSubControllers) {
				sub.init();
			}
			mInitialized = true;
			doAfterInit();
		}
		
		abstract protected void doBeforeInit() throws InitException;
		
		abstract protected void doAfterInit() throws InitException;
		
		/**
		 * Деинициализация контроллера. Процедура очистки памяти, удаления данных,
		 * закрытия всех открытых дексрипторов.
		 */
		@Override
		public synchronized void uninit() {
			logger().debug("Очистка контроллера");
			for (IController sub : mSubControllers) {
				sub.uninit();
			}
			
			if (!mInitialized) {
				logger().warn("Контроллер {} пытается сделать uninit(), но он не инициализирован! Выходим.", getClass().getSimpleName());
				return;
			}

			if (mBundleContext != null) {
				logger().info("Контекст бандала был: "+mBundleContext.toString());
				//unregisterEventHandler();
			}
			
			mInitialized = false;
		}
		
		/**
		 * Добавить дочерний контроллер в список субконтроллеров. Важно то, что если процедура 
		 * инициализации для этого контроллера уже произведена, то при добавлении субконтроллер 
		 * также инициализируется.
		 * 
		 * @param controller
		 * @throws InitException 
		 */
		@Override
		public void addSubController(IController controller) {
			if (controller == null) {
				logger().warn("Попытка добавить контроллер = null в качестве субконтроллера в {}", getClass().getSimpleName());
				return;
			}

			if (mSubControllers.contains(controller)) {
				logger().warn("Контроллер "+controller.getClass().getSimpleName()+" уже зарегистрирован как субконтроллер для этого контроллера.\n"
						+ "Возможна только одна регистрация. Выходим.");
				return;
			}

			mSubControllers.add(controller);
			
			// Если главный контроллер уже иницализирован, надо вызвать
			// процедуру инициализацию после добавления подконтроллера.
			try {
				if (mInitialized) controller.init();
			} catch (InitException ex) {
				ex.printStackTrace();
			}
		}
		
		/**
		 * Удалить дочерний контроллер.
		 * 
		 * @param index
		 */
		@Override
		public void removeSubController(int index) {
			if (mInitialized) mSubControllers.get(index).uninit();
			
			mSubControllers.remove(index);
		}

		/**
		 * Получить субконтроллер по индексу.
		 * 
		 * @param index
		 * @return субконтроллер, если есть, или null - если не задан.
		 */
		@Override
		public IController getSubController(int index) {
			return mSubControllers.get(index);
		}
	
		/**
		 * Получить количество субконтроллеров, которые добавлены к этому
		 * контроллеру.
		 * 
		 * @return количество привязанных субконтроллеров
		 */
		@Override
		public int getSubControllerCount() {
			return mSubControllers.size();
		}
		
		/**
		 * Получить сессию приложения.
		 * 
		 * @return объект сессии приложения ApplicationSession.
		 */
		@Override
		public final ApplicationSession getSession() {
			return mSession;
		}

		/**
		 * Получить класс ORM.
		 * 
		 * @return класс данных модели
		 */
		protected Class<? extends CommonObject> getDataClass() {
			return mDataClass;
		}

		/**
		 * <p>Добавить обработчик событий контроллера.
		 * Этим методом осуществляется привязка обработчика контроллера, в случае, если
		 * им управляет какой-либо головной контроллер. Обработчики связи "контроллер-контроллер"
		 * нужны для осуществления коммуникации между ними.</p>
		 * 
		 * <p>Стоит отметить, что в качетсве такого обработчика может выступать в том числе
		 * и тот обработчик, который используется в связке "субконтроллер-композит". В данном
		 * случае набор методов-событий будет, естественно, одинаковым. Однако появится
		 * возможность передавать событие от комозита субконтроллера головному контроллеру,
		 * в том числе с промежуточным преобразованием и обработкой передаваемых данных внутри
		 * субконтроллера.<p>
		 * 
		 * <p>То есть в цепочке "субкомпозит-(1)-субконтроллер-(2)-контроллер-(3)-композит"
		 * дополнительную обработку данных и логику можно добавлять в обработчике на этапе (2).</p>
		 * 
		 * @param listener экземпляр обработчика
		 */
		@Override
		public void addControllerListener(CommonControllerListener listener) {
			mListener = listener;
		}

		/**
		 * Получить обработчик событий контроллера.
		 * 
		 * @return экземпляр обработчика
		 */
		@Override
		public CommonControllerListener getControllerListener() {
			return mListener;
		}

		/**
		 * <p>Создание обработчика событий контроллера.
		 * Метод создания обработчика контроллера. На контроллере этот метод реализуется
		 * тогда, когда необходимо создать внутри контроллера какой-либо обработчик для этого
		 * контроллера. Обработчик как правило используется для связи нижестоящих компонентов
		 * с контроллером, например, чтобы осуществлять коммуникацию композита с контроллером
		 * этого композита.</p>
		 * 
		 * <p>Также, такой обработчик может быть привязан к субконтроллерам, хотя лучше не
		 * использовать его для такого рода коммуникации. Связь субконтроллеров идет по схеме
		 * "субкомпозит-(1)-субконтроллер-(2)-контроллер-(3)-композит".</p>
		 * 
		 *  <p>При этом (1) и (3) - это обработчики созданные этим методом, а (2) - это обработчик,
		 *  специально привязанный к субконтроллеру методом addControllerListener(...). При этом
		 *  реализация обработчика такой связи может быть где угодно, в коде главного 
		 *  контроллера.</p> 
		 * 
		 * @return объект вновб созданного обработчика событий контроллера
		 */
		abstract public CommonControllerListener createControllerListener();

		/**
		 * Экземпляр логгера для записи сообщений в лог.
		 * 
		 * @return объект логгера
		 */
		protected Logger logger() {
			return mLogger;
		}

		/**
		 * Установить контекст бандла для этого контроллера.
		 * 
		 * @param context контекст бандла
		 */
		public void setBundleContext(BundleContext context) {
			logger().debug("Новый контекст бандла: {}", context.toString());
			mBundleContext = context;
		}

		/**
		 * Получить контескт бандла, в котором существует этот контроллер.
		 * 
		 * @return контекст бандла
		 */
		public BundleContext getBundleContext() {
			return mBundleContext;
		}
		
		/**
		 * Отправить сообщение другому юзкейсу через шину OSGI. Сообщением
		 * может быть любой объект, но получатель должен знать, о том, какого
		 * типа объект он ожидает.
		 * 
		 * @param recieverUsecase юзкейс получателя
		 * @param event объект события
		 */
		@SuppressWarnings("unchecked")
		public void postEvent(String recieverUsecase, Object event) {
    		
			if (mBundleContext == null) {
				logger().error("Невозможно отправить сообщение от юзкейса, нет BundleContext.");
				return;
			}
			
			// Преобразуем пакет юзкейса в адресата
			recieverUsecase = recieverUsecase.replaceAll("\\.", "/")+"/Event";

			@SuppressWarnings("rawtypes")
			ServiceTracker serviceTracker = new ServiceTracker(mBundleContext, EventAdmin.class.getName(), null);
		    serviceTracker.open();

		    EventAdmin mEventAdmin = (EventAdmin) serviceTracker.getService();
		    if (mEventAdmin != null) {
	    		Dictionary<String, Object> props = new Hashtable<String, Object>();
	    		props.put("eventSender", this);
	    		props.put("eventContents", event);
	    		mEventAdmin.postEvent(new Event(recieverUsecase, props));
				logger().debug("Отпрввлено сообщение на "+recieverUsecase);
		    } else {
		    	logger().error("Нет сервиса EventAdmin в системе!");
		    }
			
		}
		
		/**
		 * Обработать сообщение от другого контроллера, принятое по OSGI.
		 * Необходимо этот обработчик переопределить на дочернем классе.
		 * 
		 * @param sender - контроллер отправителя
		 * @param event - объект сообщения
		 */
		public void handleEvent(CommonController sender, Object event) {
			logger().debug("Полчуено сообщение '{}' по OSGI от {}", event.toString(), sender.getClass().getSimpleName());
		}

		/**
		 * Метод запускающий юзкейсы. Любой контроллер может запустить из себя юзкейс.
		 * Вообще юзкейс в даннос случае - это просто способ использования контроллера,
		 * а также юзкейс - это архитектурный набор из контроллера и композита, ограниченный 
		 * одним бандлом.
		 * 
		 * Заупск юзкейса преставляет собой простой запуск контроллера и
		 * его инициализацию. При этом композит контроллера будет присобачен в контейнер,
		 * который указан при создании контроллера.
		 * 
		 * Для того, чтобы контроллер работал в режиме юзкейса нужно, чтобы он
		 * имел соответствующий конструктор (режим субконтроллера).
		 * 
		 * @param usecaseBundle название юзкейса, зарегистрированного в реестре юзкейсов
		 * @param dataClass класс данных, для работы этого юзкейса
		 * @return объект контроллера запущенного юзкейса в случае удачи, в случае неудачи null.
		 */
		public CompositeController handleRunUsecase(String usecaseBundle, Class<?> dataClass) {		
			logger().debug("Контекст бандла для запуска юзкейса: {}", mBundleContext.toString());			
			if (mBundleContext == null) {
				handleError("Невозможно запустить юзкейс "+usecaseBundle+", неизвестен контекст бандла.", null);
				return null;
			}

			Class<? extends CompositeController> usecaseController = 
					getUsecaseControllerClass(usecaseBundle);

			if (usecaseController != null) {
				// Запустим контроллер юзкейса в работу
				return handleRunControllerAsUsecase(usecaseController, dataClass, null);
			}
			
			return null;
		}

		/**
		 * Запуск любого контроллера композита в виде юзкейса. Вообще, запуск юзкейса это
		 * создание экземпляра контроллера и его внедрение в качестве субконтроллера, что и
		 * происходит в данном случае.
		 * 
		 * Если необходимо запустить контроллер в указанном контейнере, нужно передать container.
		 * Если container будет null, то для запуска будет использоваться стандартный контейнер
		 * этого контроллера. В первом случае, у контроллера, который будет запущен должен быть
		 * определен конструктор с параметрами (parentController, dataClass, params), а во втором
		 * (parentController, dataClass, container, params).
		 *  
		 * @param controller
		 * @param dataClass
		 * @param container
		 * @return объект контроллера запущенного юзкейса в случае удачи, в случае неудачи null. 
		 */
		public CompositeController handleRunControllerAsUsecase(
				Class<? extends CompositeController> controller,
				Class<?> dataClass,
				CommonComposite container) {

			CompositeController c = getUsecaseController(controller, dataClass, container, new CompositeParams());

			// Добавляем субконтроллер для текущего контроллера
			addSubController(c);
				
			return c;			
		}
		
		public final Class<? extends CompositeController> getUsecaseControllerClass(String usecaseBundle) {
			logger().debug("Контекст бандла для запуска юзкейса: {}", mBundleContext.toString());
			if (mBundleContext == null) {
				handleError("Невозможно запустить юзкейс "+usecaseBundle+", неизвестен контекст бандла.", null);
				return null;
			}

			// Получим экземпляр сервиса реестра юзкейсов
			UseCaseRegister register = (UseCaseRegister) mBundleContext.getService(
					mBundleContext.getServiceReference(UseCaseRegister.class.getName()));

			// Получим контроллен юзкейса
			@SuppressWarnings("unchecked")
			Class<? extends CompositeController> usecaseController = 
					(Class<? extends CompositeController>)register.getController(usecaseBundle);

			return usecaseController;
		}
		
		public final CompositeController getUsecaseController(
				Class<? extends CompositeController> usecaseController,
				Class<?> dataClass,
				Composite container,
				CompositeParams params) {

			if (usecaseController == null) return null;
			
			try {
				// Возьмем нужный конструктор контроллера, если надо запустить его
				// в композите по-умолчанию для этого контроллера, то контейнер указывать
				// не надо, то есть он будет null. Если нужно указать композит для упаковки
				// юзкейса, то контейнер нужно указать явно.				
				Constructor<? extends CommonController> constr;
				if (container == null) {
					constr = usecaseController.getConstructor(
						CompositeController.class,
						Class.class,
						CompositeParams.class
						);
				} else {
					constr = usecaseController.getConstructor(
						CompositeController.class,
						Class.class,
						Composite.class,
						CompositeParams.class
						);															
				}
				logger().debug("Контекст бандла для запуска юзкейса: {}", mBundleContext.toString());
				if (mBundleContext == null) {
					handleError("Невозможно запустить юзкейс "+usecaseController.getName()+", неизвестен контекст бандла.", null);
					return null;
				}

				logger().info("Запуск контроллерак юзкейса: {}", usecaseController.getName());
				CompositeController c;
				if (container == null) {
					c = (CompositeController) constr.newInstance(this, dataClass, params);
				} else {
					// Очистить контейнер нннада
					c = (CompositeController) constr.newInstance(this, dataClass, container, params);
				}
				c.setBundleContext(mBundleContext);
				
				return c;
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				String errorText = "Невозможно создать контроллер.";
				handleError(errorText, ex);
			} catch (InstantiationException | NoSuchMethodException  ex) {
				String errorText = "Юзкейс не может быть запущен. Проверьте наличие в контроллере соответствующего конструктора.";
				handleError(errorText, ex);
			}

			return null;			
		}
		
		public final CompositeController getUsecaseController(
				String usecaseBundle,
				Class<? extends CommonObject> dataClass,
				Composite container,
				CompositeParams params) {

			Class<? extends CompositeController> usecaseController = 
					getUsecaseControllerClass(usecaseBundle);

			return getUsecaseController(usecaseController, dataClass, container, params);
		}
		
		/**
		 * Обновление данных контроллера. Вызывает обновление всех субконтроллеров.
		 * <p>Можно переопределить на дочерних классах, чтобы обработать обновление данных
		 * каким-либо своим образом.</p>
		 * 
		 * @param refreshSubcontrollers
		 * @throws DTOException 
		 */
		@Override
		public void refresh(boolean refreshSubcontrollers) throws Exception {
			if (refreshSubcontrollers) {
				logger().debug("Refreshing subcontrollers...");
				for (int i = 0; i < getSubControllerCount(); i++) {
					getSubController(i).refresh(refreshSubcontrollers);
				}
			}
		}
		
		/**
		 * Отправка почты через MailerService.
		 * 
		 * @param reciever получатель
		 * @param subject тема письма
		 * @param body тело письма
		 * @param attachments вложения в письмо
		 */
		@Override
		public void sendMail(String reciever, String subject, String body,
			HashMap<String, InputStream> attachments) {
			if (mBundleContext == null) {
				mLogger.error("Can't send email from "+getClass().getSimpleName()+": bundle context is unavailable.");
				return;
			}
		    EventAdmin eventAdmin = (EventAdmin) mBundleContext.getService(
		    		mBundleContext.getServiceReference(EventAdmin.class)
		    		);
		    		    
		    // Послать сообщение мейлеру об отправке почты
		    if (eventAdmin != null) {
		    	if (attachments == null) { attachments = new HashMap<String, InputStream>(); }
		    	Dictionary<String, Object> props = new Hashtable<String, Object>();
		    	props.put("reciever", reciever);
		    	props.put("subject", subject);
		    	props.put("body", body);
		    	props.put("attachments", attachments);
		    	eventAdmin.postEvent(new Event("ru/futurelink/mo/mailer/Send", props));
		    } else {
		    	mLogger.error("Can't send email because of EventAdmin is unavailable!");		   
		    }
		}
		
		/**
		 * Из любого места, где доступен контроллер, в том числе и из журнала юзкейсов
		 * может быть вызван этот метод.
		 * 
		 * @param event
		 */
		public void execute(String event) {}
		
		/**
		 * Вернуть экземпляр самого себя.
		 * 
		 * @return
		 */
		public CommonController getSelf() {
			return this;
		}
}

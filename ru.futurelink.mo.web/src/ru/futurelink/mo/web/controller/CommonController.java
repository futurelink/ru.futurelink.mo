package ru.futurelink.mo.web.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.iface.IBundleDecorator;
import ru.futurelink.mo.web.controller.iface.IController;
import ru.futurelink.mo.web.controller.iface.IMailSender;
import ru.futurelink.mo.web.controller.iface.ISessionDecorator;
import ru.futurelink.mo.web.exceptions.InitException;

public abstract class CommonController 
		implements IController, ISessionDecorator, IBundleDecorator, IMailSender 
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
			if (!mInitialized) {
				// Log session ID for debugging purposes
				logger().info("JSESSIONID={}, user={}", 
						getSession().getId(), 
						getSession().getUser() != null ? getSession().getUser().getId() : null
					);
			
				logger().debug("Инициализация контроллера");
				if (mBundleContext != null) {
					logger().debug("Мой контекст бандала: "+mBundleContext.toString());
				//registerEventHandler();
				} else 
					logger().info("Нет контекста бандла, запущен без взаимодействия OSGI.");

				// Check session user existance, user must exist in session scope
				/*if (mSession.getUser() == null) {
					throw new InitException("В сессии отсутствует пользователь, вероятно она закончилась.");
				}*/

				doBeforeInit();
				for (IController sub : mSubControllers) {
					sub.init();
				}
				mInitialized = true;
				doAfterInit();
			} else {
				logger().warn("Контроллер зачем-то пытается вызвать init() хотя он уже инициализирован!");
			}
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
				if (getInitialized()) controller.init();
			} catch (InitException ex) {
				ex.printStackTrace();
			}
		}
		
		@Override
		public boolean getInitialized() {
			return mInitialized;
		}
		
		/**
		 * Удалить дочерний контроллер.
		 * 
		 * @param index
		 */
		@Override
		public void removeSubController(int index) {
			if (mSubControllers.get(index).getInitialized()) mSubControllers.get(index).uninit();
			
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

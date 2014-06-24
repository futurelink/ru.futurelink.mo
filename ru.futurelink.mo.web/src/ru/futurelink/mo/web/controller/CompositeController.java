package ru.futurelink.mo.web.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.composites.fields.datapicker.CommonDataPickerController;
import ru.futurelink.mo.web.composites.fields.datapicker.DataPicker;
import ru.futurelink.mo.web.composites.fields.datapicker.SimpleDataPickerController;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;
import ru.futurelink.mo.web.register.UseCaseRegister;

/**
 * Abstract class for composite controller.
 * 
 * @since 1.2
 */
public abstract class CompositeController 
	extends CommonController 
	implements ICompositeController 
{
	protected 	CommonComposite 		mComposite;
	protected	Composite				mContainer;
	
	private		CompositeParams			mCompositeParams;
	private 	ICompositeController	mParentController;

	private		String					mNavigationTag;
	private		String					mNavigationTitle;

	/**
	 * Конструктор, который может использоваться для создания, например, ApplicationController,
	 * конструктор защищенный, использование его на прикладном уровне невозможно.
	 * 
	 * @param session сессия приложения
	 * @param dataClass класс данных модели
	 */
	protected CompositeController(ApplicationSession session, Class<? extends CommonObject> dataClass) {
		super(session, dataClass);
	}
	
	/**
	 * Конструктор для создания контроллера юзкейса не упакованного в какой-либо контейнер.
	 * 
	 * @param parentController родительский контроллер
	 * @param dataClass класс данных модели
	 * @param compositeParams параметры передаваемые композиту
	 */
	public CompositeController(ICompositeController parentController, Class<? extends CommonObject> dataClass,
			CompositeParams compositeParams) {
		super(parentController.getSession(), dataClass);

		doAfterConstruct(parentController, compositeParams);
		mContainer = mParentController.getComposite(); // Контейнер для композита берем из родителя		
	}

	/**
	 * Этот конструктор реализуется тогда, когда контроллер должен уметь работать
	 * как субпонтроллер, а композит его должен быть упакован в контейнер, определяемый
	 * параметром container. 
	 * 
	 * Без реализации этого конструктора работа в режиме субконтроллера невозможна!
	 * 
	 * @param parentController родительский контроллер
	 * @param dataClass класс данных модели
	 * @param container композит-контейнер для композита этого контроллера
	 */
	public CompositeController(ICompositeController parentController, Class<? extends CommonObject> dataClass, 
			Composite container, CompositeParams compositeParams) {
		super(parentController.getSession(), dataClass);

		doAfterConstruct(parentController, compositeParams);
		mContainer = container;	// Контейнер для композита указан явно
	}
	
	/**
	 * Приватный метод, который надо вызывать после создания объекта в конструкторе.
	 * Общая для всех инициализация.
	 * 
	 * @param parentController родительский контроллер
	 * @param params параметры, передаваемые композиту
	 */
	private void doAfterConstruct(ICompositeController parentController, CompositeParams params) {
		mParentController = parentController;

		if (mParentController.getBundleContext() != null) {
			setBundleContext(mParentController.getBundleContext());
		}

		mCompositeParams = params;

		/*if (params == null) {
			mCompositeParams = new CompositeParams();
		} else {
			mCompositeParams = params;
		}*/
	}

	/**
	 * ВЫполняет инициализацию контроллера, но до нее выполняет создание композита,
	 * в методе createComposite(CompositeParams params).
	 * 
	 * @see #createComposite(CompositeParams params)
	 */
	@Override
	public synchronized void init() throws InitException {
		// Форма должна быть создана до того, как будут инициализироваться
		// вложенные контроллеры.
		if (!mInitialized)
			try {
				creation(mCompositeParams);
				if ((params() != null) && (getComposite() == null)) {
					throw new InitException("Ошибка инициализации: не создан композит окна.");
				}
			} catch (CreationException ex) {
				logger().error(ex.getMessage(), ex);
				throw new InitException(ex.getMessage()); 
			}

		// Инициализация для вложеннных контроллеров из CommonController.
		super.init();
	};
	
	/**
	 * Создание композита для контроллера.
	 * 
	 * Бывает так, что контроллеру не нужен композит, напрмер, если
	 * надо просто создать объект на основе входных данных и ничего больше
	 * не показывать пользователю. На этот случай есть разные проверки,
	 * которые не инициализируют композит и не проверяют наличие обработчика
	 * событий от композита.
	 * 
	 * @param compositeParams
	 */
	private synchronized void creation(CompositeParams compositeParams) throws CreationException {
		/*if (compositeParams == null)
			compositeParams = new CompositeParams();*/

		mCompositeParams = compositeParams;

		// Создаем композит для контроллера
		doBeforeCreateComposite();
		CommonComposite c = createComposite(compositeParams);
		try {
			if (c != null) {
				c.init();
			} else {
				if (compositeParams != null) {
					throw new CreationException("Произошла ошибка при инициализации композита: композит не создан");					
				} else {
					logger().info("Composite was not created and it is null. This state is ignored because"
							+ "of composite params is passed null.");
				}
			}
		} catch (InitException ex) {
			if (c != null) {
				if (!c.isDisposed()) c.dispose();
				c = null;
			}
			if (compositeParams != null)
				throw new CreationException("Произошла ошибка при инициализации композита: "+ex.getMessage());
		}
		setComposite(c);
		doAfterCreateComposite();

		// Анализ созданной связки контроллер-композит и выдача соответствующих
		// предупреждений.
		if ((getComposite() != null) && (getComposite().getControllerListener() == null)) {
			logger().warn("Для композита этого контроллера не задан или не создан обработчик, если композит используется с"
					+ "контроллером, то обработчик должен быть создан.");
		}
	}
		
	/**
	 * Метод нужно переопределить, для создания композита для этого контроллера.
	 * После создания композита необходимо привязать к нему обработчик событий с
	 * помощью addControllerListener().
	 * 
	 * @param params параметры, которые переданы композиту (из конструктора)
	 * @return объект композита CommonComposite
	 * 
	 * @see addControllerListener()
	 */
	protected abstract CommonComposite createComposite(CompositeParams params);
	
	/**
	 * Метод нужно переопределить, процедуры выполнающиеся ДО создания композита.
	 */
	protected abstract void doBeforeCreateComposite();

	/**
	 * Метод нужно переопределить, процедуры выполнающиеся ПОСЛЕ создания композита.
	 */	
	protected abstract void doAfterCreateComposite();
		
	/**
	 * Получить композит контейнера для композита контроллера.
	 * 
	 * @return контейнер композита для данного контроллера
	 */
	public Composite getContainer() {
		return mContainer;
	}
	
	/**
	 * Получить привязанный композит.
	 * 
	 * @return композит данного контроллера
	 */
	public CommonComposite getComposite() {
		return mComposite;
	}

	/**
	 * Привязать композит к контроллеру. Защищенный метод, используется только 
	 * внутри контроллера и его наследников.
	 * 
	 * @param composite объект композита
	 */
	protected synchronized final void setComposite(CommonComposite composite) {
		// Если композит меняется сам на себя, вдруг, то диспозить его не надо,
		// и вообще ничего делать не надо.
		if (composite == mComposite) return;
		
		if (mComposite != null) {
			mComposite.dispose();
			mComposite = null;
		
			System.gc();
		}
		
		mComposite = composite;

		// Переразмещаем компоненты в контейнере
		if (mContainer != null && !mContainer.isDisposed())
			mContainer.layout();
		else
			logger().warn("setComposite(): Контейнер, в которй должен быть упакован композит окна почему-то disposed или null...");
	}

	/**
	 * Получить родительский контроллер.
	 * 
	 * @return объект родительского CompositeController
	 */
	public final ICompositeController getParentController() {
		return mParentController;
	}

	/**
	 * Изменяет родителя комопзита дочернего CompositeController, перемещает
	 * его отображение на другой композит. <i>Метод не работает в RAP.</i>
	 * 
	 * @param newParent новый родительский композит.
	 */
	@Override
	public void reparentComposite(Composite newParent) {
		if (getComposite() != null) {
			Composite oldParent = getComposite().getParent();
			if (getComposite().setParent(newParent)) {
				newParent.pack();
				oldParent.pack();
			} else {
				handleError("Смена родителя не поддерживается платформой!", null);
			}
		} else {
			handleError("Не могу установить родителя композита контроллера в null!", null);
		}
	}
	
	/**
	 * Общий для всех контроллеров метод обработки ошибок уровня пользователя.
	 * Для этого контроллера - выводит окно с сообщением об ошибке.
	 * 
	 * @param errorText текст ошибки
	 * @param exception объект Exception, или null если отсутствует
	 */
	@Override
	public void handleError(String errorText, Exception exception) {
		// Оишбкм связанные с нехваткой прав доступа логируем отдельно,
		// в общий лог их сыпать не надо.
		if ((exception != null)) {
			if (!DTOAccessException.class.isAssignableFrom(exception.getClass())) {
				logger().error(errorText, exception);
			} else {
				// Ошибка доступа логгируем отдельно
				logger().info(((DTOAccessException)exception).getAccessData());
				logger().info(errorText, exception);
			}
		}

		// Показать месседжбокс нннада
		if (getComposite() != null) {
			MessageBox msg = new MessageBox(getComposite().getShell(), SWT.ICON_ERROR);
			msg.setText(getComposite().getLocaleString("systemError"));
			msg.setMessage(errorText+"\n"+
				(exception != null ? exception.getMessage() : ""));
			msg.open();
		}
	}

	/**
	 * Метод переопределн для того, чтобы удаление контроллера
	 * приводило к удалению связанного с ним композита.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (getComposite() != null)
			getComposite().dispose();
		super.finalize();
	}
	
	/**
	 * Получить параметры композита данного контроллера. Метод защищенный, использовать
	 * можно только внутри контроллера.
	 * 
	 * @return параметры, которые передаются композиту при создании
	 */
	protected CompositeParams params() {
		return mCompositeParams;
	}
	
	/**
	 * Добавить поддержку драг-дропа для композита этого контроллера, при этом
	 * добавляется и листенер драг-дропа.
	 * 
	 * @param operations
	 * @param transferTypes
	 * @param listener
	 */
	@Override
	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener) {
		if (IDragDropDecorator.class.isAssignableFrom(getComposite().getClass())) {
			((IDragDropDecorator)getComposite()).addDropSupport(operations, transferTypes, listener);
		}
	}

	/**
	 * Очистить композит данного контроллера и удалить все композиты, которые в нем расположены.
	 * Сначала выполняется очистка окна, а потом логическое удаление субконтроллеров.
	 */
	@Override
	public void clear() {
		logger().info("Очистка композита...");
		if (getComposite() != null && !getComposite().isDisposed()) {
			Control[] controls = getComposite().getChildren();
			for (int i = 0; i < controls.length; i++) {
				if(controls[i] != null) {
					controls[i].dispose();
				}
			}
		}

		logger().info("Удаление субконтроллеров...");
		for (int i = 0; i < getSubControllerCount(); i++) {
			removeSubController(i);
		}

	}
	
	public CommonComposite createErrorComposite(String message) {
		CommonComposite c = new CommonComposite(getSession(), getContainer(), SWT.NONE, new CompositeParams());
		c.setLayout(new FillLayout());
		
		Label l = new Label(c, SWT.NONE);
		l.setText(message);
		
		return c;
	}

	/**
	 * Обработка открытия окна выбора из базы данных посредством
	 * поля DataPicker.
	 * 
	 * @param picker
	 */
	public void handleOpenDataPickerDialog(DataPicker picker) {
		CommonDialog d = new CommonDialog(getSession(), getComposite().getShell(), SWT.NONE);		
		Class<? extends CommonDataPickerController> pickerControllerClass = picker.getPickerController();
		if (pickerControllerClass == null) {
			pickerControllerClass = SimpleDataPickerController.class;
		}

		// Вытащим конструктор окна выбора
		Constructor<?> constr;
		try {
			constr = pickerControllerClass.getConstructor(
					CompositeController.class,
					Class.class,
					Composite.class,
					CompositeParams.class);
		} catch (NoSuchMethodException | SecurityException ex1) {
			handleError("Ошибка получения конструктора для окна выбора из списка.", ex1);
			return;
		}
		
		d.setText("Выбор элмента");				
		try {		
			CommonDataPickerController c = (CommonDataPickerController) constr.newInstance(
					(CompositeController) mThisController,
					picker.getDataClass(),
					d.getShell(),
					(new CompositeParams()).
					
						// Параметры выборки пикера
						add("tableClass", picker.getTableClass()).
						add("queryConditions", picker.getQueryConditions()).
						add("orderBy", picker.getOrderBy()).
						
						// Параметры, которые касаются возможностей пикера
						add("itemControllerClass", picker.getItemControllerClass()).
						add("itemDialogParams", picker.getItemDialogParams()).
						add("allowCreate", picker.getAllowCreate()).
						add("public", picker.getPublic())
						);
			c.init();
			d.attachComposite(c.getComposite());
			d.setSize(CommonDialog.LARGE);			
			d.open();
			
			// Пикеру просетили элемент DTO чтобы он уже отобразил данные на своем поле
			if ((d.getResult() != null) && (d.getResult().equals("save"))) {
				try {
					picker.setSelectedDTO(c.getActiveData());
					picker.refresh();
				} catch (DTOException ex) {
					// TODO handle this error!
				}
			}
		} catch (IllegalArgumentException ex) {
			handleError("Ошибка создания диалога выбора из справочника.", ex);
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | InitException ex) {
			handleError("Ошибка создания диалога выбора из справочника.", ex);
		}
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
	 * Установить навигационную метку. Метка будет отображаться как #tag в 
	 * строке адреса браузера.
	 * 
	 * @param tag
	 */
	public void setNavigationTag(String tag) {
		mNavigationTag = tag;
	}
	
	/**
	 * Установить навигационный заголовок. В купе с меткой он будет использоваться для
	 * навигации кнопками браузера и отображаться в истории.
	 * 
	 * @param title
	 */
	public void setNavigationTitle(String title) {
		mNavigationTitle = title;
	}
	
	public String getNavigationTag() {
		return mNavigationTag;
	}
	
	public String getNavigationTitle() {
		if (mNavigationTitle != null)
			return mNavigationTitle;
		return mNavigationTag;
	}
	
	public void close() {
		uninit();
		setComposite(null);		
		doAfterClose();
	}
	
	public void doAfterClose() {}
}


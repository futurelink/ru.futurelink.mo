/*******************************************************************************
 * Copyright (c) 2013-2014 Pavlov Denis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pavlov Denis - initial API and implementation
 ******************************************************************************/

package ru.futurelink.mo.web.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import ru.futurelink.mo.orm.dto.access.AccessChecker;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.composites.fields.datapicker.CommonDataPickerController;
import ru.futurelink.mo.web.composites.fields.datapicker.DataPicker;
import ru.futurelink.mo.web.composites.fields.datapicker.SimpleDataPickerController;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IControllerRefreshHandler;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;
import ru.futurelink.mo.web.register.UseCaseInfo;
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

	private		String					mNavigationTitle;
	private		String					mNavigationTag;

	/**
	 * Конструктор, который может использоваться для создания, например, ApplicationController,
	 * конструктор защищенный, использование его на прикладном уровне невозможно.
	 * 
	 * @param session сессия приложения
	 * @param dataClass класс данных модели
	 */
	protected CompositeController(ApplicationSession session, Class<? extends ICommonObject> dataClass) {
		super(session, dataClass);
	}
	
	/**
	 * Конструктор для создания контроллера юзкейса не упакованного в какой-либо контейнер.
	 * 
	 * @param parentController родительский контроллер
	 * @param dataClass класс данных модели
	 * @param compositeParams параметры передаваемые композиту
	 */
	public CompositeController(ICompositeController parentController, Class<? extends ICommonObject> dataClass,
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
	public CompositeController(ICompositeController parentController, Class<? extends ICommonObject> dataClass, 
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

	protected final IDTOAccessChecker createAccessChecker() {
		// If there is an annotation of AccessChecker, try to create checker instance
		if (getClass().getAnnotation(AccessChecker.class) != null) {
			Class<? extends IDTOAccessChecker> checkerClass = 
				getClass().getAnnotation(AccessChecker.class).checker();

			IDTOAccessChecker checker = null; 
			try {
				checker = checkerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				logger().error("Cannot create IDTOAccessChecker from annotation @AccessChecker", ex);
			}

			// Initialize access checker with param methods,
			// if execution is impossible use nulls as init params..
			String[] methods = getClass().getAnnotation(AccessChecker.class).params();
			List<Object> methParams = new ArrayList<Object>();
			for (String method : methods) {
				try {
					Method meth = getClass().getMethod(method);
					methParams.add(meth.invoke(this));
				} catch (NoSuchMethodException | SecurityException ex) {
					methParams.add(null);
					logger().error("No such method method {} on {}, so NULL will be used", 
							method, getClass().getSimpleName());
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException ex) {
					methParams.add(null);
					logger().error("Could not execute method {} on {}, so NULL will be used", 
							method, getClass().getSimpleName());
				}
			}
			
			// And call init with execution results
			checker.init(methParams.toArray());
			
			return checker;
		}
		
		return null;
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
	@Override
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
		Composite parent = null;
		if (getComposite() != null && !getComposite().isDisposed()) {
			parent = getComposite();
		} else if(getContainer() != null && !getContainer().isDisposed()) {
			parent = getContainer();
		}
		if (parent != null) {
			MessageBox msg = new MessageBox(parent.getShell(), SWT.ICON_ERROR);
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
		logger().info("Clear composite...");
		if (getComposite() != null && !getComposite().isDisposed()) {
			Control[] controls = getComposite().getChildren();
			for (int i = 0; i < controls.length; i++) {
				if(controls[i] != null) {
					controls[i].dispose();
				}
			}
		}
		
		logger().info("Remove subcontrollers...");
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
		
		CommonDataPickerController c = null;
		d.setText(getComposite().getLocaleString("selection"));				
		try {		
			c = (CommonDataPickerController) constr.newInstance(
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
			// If there was an error in composite creation it must do nothing
			if (c.getComposite() != null && !c.getComposite().isDisposed()) {
				d.attachComposite(c.getComposite());
				if (getSession().getMobileMode()) {
					// In mobile mode we use fullscreen list sizing
					d.setSize(CommonDialog.FULL);
				} else {
					// In desktop mode we use screen-relative list sizing
					d.setSize(CommonDialog.LARGE);
				}
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
			}
		} catch (IllegalArgumentException ex) {
			handleError("Ошибка создания диалога выбора из справочника.", ex);
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | InitException ex) {
			handleError("Ошибка создания диалога выбора из справочника.", ex);
		} finally {
			constr = null;
			c = null;
			d = null;			
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
	 * DEPRECATED. Use handleRunUsecase(String usecaseBundle) instead.
	 * 
	 * @param usecaseBundle название юзкейса, зарегистрированного в реестре юзкейсов
	 * @param dataClass класс данных, для работы этого юзкейса
	 * @return объект контроллера запущенного юзкейса в случае удачи, в случае неудачи null.
	 */
	/*@Deprecated
	@SuppressWarnings("unchecked")
	public CompositeController handleRunUsecase(
			String usecaseBundle, 
			Class<?> dataClass) {
		// Запустим контроллер юзкейса в работу
		CompositeController c = getUsecaseController(
				usecaseBundle, 
				(Class<? extends CommonObject>)dataClass, 
				null, 
				new CompositeParams()
			);
		if (c != null) {
			// Добавляем субконтроллер для текущего контроллера
			addSubController(c);			
			return c;
		}
		
		return null;
	}*/

	/**
	 * Runs usecase bundle in current controller composite using usecase parameters.
	 * 
	 * @param usecaseBundle
	 * @param usecaseParams
	 * @return
	 */
	public CompositeController handleRunUsecase(String usecaseBundle, 
			Map<String, Object> usecaseParams, boolean clearBeforeRun) {
		CompositeParams params = new CompositeParams();
		params.add("usecaseParams", usecaseParams);
		
		if (clearBeforeRun)
			clear();

		logger().info("Usecase {} starting with params {}", usecaseBundle, usecaseParams);

		CompositeController c = getUsecaseController(
				usecaseBundle, 
				null, 
				params
			);
		if (c != null) {
			addSubController(c);

			// Save navigation state to browser
			if (c.getNavigationTag() != null) {
				try {
					BrowserNavigation navigation = RWT.getClient().getService(BrowserNavigation.class);
					navigation.pushState(c.getNavigationString(), c.getNavigationTitle());
				} catch (Exception ex) {
					logger().warn("Couldn't save navigation state for {}", c.getNavigationTag());
				}
			}

			c.processUsecaseParams();

			return c;
		}

		return null;		
	}

	/**
	 * Runs usecase bundle in current controller composite.
	 * 
	 * @param usecaseBundle
	 * @return
	 */
	public CompositeController handleRunUsecase(String usecaseBundle, boolean clearBeforeRun) {
		return handleRunUsecase(usecaseBundle, (Map<String,Object>)null, clearBeforeRun);
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
		CompositeController c = createUsecaseController(
				controller, 
				dataClass, 
				container, 
				new CompositeParams()
			);

		// Добавляем субконтроллер для текущего контроллера
		addSubController(c);
			
		return c;			
	}

	/**
	 * Get usecase bundle name by navigation tag.
	 * 
	 * @param tag
	 * @return
	 */
	public final String getUsecaseByNavigationTag(String tag) {
		// Get usecase register service
		UseCaseRegister register = (UseCaseRegister) mBundleContext.getService(
				mBundleContext.getServiceReference(UseCaseRegister.class.getName()));

		if (register == null) {
			handleError("Cannot get usecase, register service is unavailable.", null);
			return null;
		}
		
		return register.getUsecaseByNavigationTag(tag);
	}
	
	/**
	 * 
	 * @param usecaseBundle
	 * @return
	 */
	public final UseCaseInfo getUsecaseInfo(String usecaseBundle) {
		logger().debug("Bundle context for usecase is {}", mBundleContext.toString());
		if (mBundleContext == null) {
			handleError("Cannot get usecase info "+usecaseBundle+", bundle context is not set.", null);
			return null;
		}

		// Get usecase register service
		UseCaseRegister register = (UseCaseRegister) mBundleContext.getService(
				mBundleContext.getServiceReference(UseCaseRegister.class.getName()));
		if (register == null) {
			handleError("Cannot get usecase, register service is unavailable.", null);
			return null;
		}

		// Get usecase info from usecase register
		return register.getInfo(usecaseBundle);
	}
	
	/**
	 * 
	 * @param usecaseBundle
	 * @return
	 */
	public final Class<? extends CompositeController> getUsecaseControllerClass(String usecaseBundle) {
		UseCaseInfo info = getUsecaseInfo(usecaseBundle);
		if (info == null) return null;

		// Get usecase controller from usecase register
		@SuppressWarnings("unchecked")
		Class<? extends CompositeController> usecaseController = 
				(Class<? extends CompositeController>)info.getControllerClass();

		return usecaseController;
	}
	
	@Deprecated
	public final CompositeController getUsecaseController(
			String usecaseBundle,
			Class<? extends ICommonObject> dataClass,
			Composite container,
			CompositeParams params) {

		// Retrieve usecase information
		UseCaseInfo info = getUsecaseInfo(usecaseBundle);
		if (info == null) return null;

		// Get usecase controller from usecase register
		@SuppressWarnings("unchecked")
		Class<? extends CompositeController> usecaseControllerClass = 
				(Class<? extends CompositeController>)info.getControllerClass();
		if (usecaseControllerClass == null) return null;

		CompositeController usecaseController = 
				createUsecaseController(
						usecaseControllerClass, 
						dataClass, 
						container, 
						params
					);
		
		// Set controller navigation tag
		usecaseController.setNavigationTag(info.getNavigationTag());
		
		return usecaseController;
	}

	public final CompositeController getUsecaseController(
			String usecaseBundle,
			Composite container,
			CompositeParams params) {

		// Retrieve usecase information
		UseCaseInfo info = getUsecaseInfo(usecaseBundle);
		if (info == null) return null;

		// Get usecase controller from usecase register
		@SuppressWarnings("unchecked")
		Class<? extends CompositeController> usecaseControllerClass = 
				(Class<? extends CompositeController>)info.getControllerClass();
		if (usecaseControllerClass == null) return null;

		CompositeController usecaseController = 
				createUsecaseController(
						usecaseControllerClass, 
						info.getDataClass(), 
						container, 
						params
					);

		// Set controller navigation tag
		if (usecaseController != null)
			usecaseController.setNavigationTag(info.getNavigationTag());
		
		return usecaseController;
	}

	public final CompositeController createUsecaseController(
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
					ICompositeController.class,
					Class.class,
					CompositeParams.class
					);
			} else {
				constr = usecaseController.getConstructor(
					ICompositeController.class,
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
	
	/**
	 * Установить навигационный заголовок. В купе с меткой он будет использоваться для
	 * навигации кнопками браузера и отображаться в истории.
	 * 
	 * @param title
	 */
	public void setNavigationTitle(String title) {
		mNavigationTitle = title;
	}
	
	/**
	 * Returns navigation title (browser title) for controller ran as usecase.
	 * 
	 * @return
	 */
	public String getNavigationTitle() {
		if (mNavigationTitle != null)
			return mNavigationTitle;
		return "";
	}
	
	private void setNavigationTag(String tag) {
		mNavigationTag = tag;
	}
	
	/**
	 * Returns navigation tag for controller ran as usecase.
	 * 
	 * @return
	 */
	public String getNavigationTag() {
		return mNavigationTag;
	}
	
	/**
	 * Reimplement this to generate navigation string (combine necessary tag and parameters).
	 * 
	 * @return
	 */
	public String getNavigationString() {
		return getNavigationTag();
	}
	
	public void close() {
		uninit();
		setComposite(null);		
		doAfterClose();
	}
	
	public void doAfterClose() {}
	
	/**
	 * Get usecase params.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUsecaseParams() {		
		return (Map<String, Object>)params().get("usecaseParams"); 
	}
	
	@Override
	public void processUsecaseParams() {}
	
	@Override
	public void refreshBySender(String sender, boolean refreshSubcontrollers) throws Exception {
		// If this controller is filtered refreshable and refresh filter is valid,
		// refresh it.
		if (IControllerRefreshHandler.class.isAssignableFrom(getClass())) {
			if (((IControllerRefreshHandler)this).getRefreshBySenderFilters().equals(sender)) {
				refresh(false);
			}
		}
		
		// Propagate executuion on subcontrollers if needed
		if (refreshSubcontrollers) {
			logger().info("Refreshing subcontrollers by sender...");
			for (int i = 0; i < getSubControllerCount(); i++) {
				if (CompositeController.class.isAssignableFrom(getSubController(i).getClass()))
					((CompositeController)getSubController(i)).
						refreshBySender(sender, refreshSubcontrollers);
			}
		}
	}
}


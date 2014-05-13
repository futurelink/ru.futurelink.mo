package ru.futurelink.mo.web.controller;

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
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
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

		if (params == null) {
			mCompositeParams = new CompositeParams();
		} else {
			mCompositeParams = params;
		}
	}

	/**
	 * ВЫполняет инициализацию контроллера, но до нее выполняет создание композита,
	 * в методе createComposite(CompositeParams params).
	 * 
	 * @see #createComposite(CompositeParams params)
	 */
	@Override
	public void init() throws InitException {
		// Форма должна быть создана до того, как будут инициализироваться
		// вложенные контроллеры.
		if (!mInitialized)
			try {
				creation(mCompositeParams);
				if (getComposite() == null) {
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
	private void creation(CompositeParams compositeParams) throws CreationException {
		if (compositeParams == null)
			compositeParams = new CompositeParams();

		mCompositeParams = compositeParams;

		// Создаем композит для контроллера
		doBeforeCreateComposite();
		CommonComposite c = createComposite(compositeParams);
		try {
			if (c != null) {
				c.init();
			} else {
				throw new CreationException("Произошла ошибка при инициализации композита: композит не создан");
			}
		} catch (InitException ex) {
			if (c != null) {
				if (!c.isDisposed()) c.dispose();
				c = null;
			}
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
	protected final void setComposite(CommonComposite composite) {
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
		if ((exception != null) && !DTOAccessException.class.isAssignableFrom(exception.getClass())) {
			logger().error(errorText, exception);
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
		if (getComposite() != null)
			getComposite().dispose();	
	}
}

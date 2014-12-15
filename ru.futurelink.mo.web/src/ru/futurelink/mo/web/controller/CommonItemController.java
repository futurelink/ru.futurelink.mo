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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.exceptions.ValidationException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IModelObject;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;
import ru.futurelink.mo.orm.pm.PersistentObjectFactory;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.RelatedController.SaveMode;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IItemController;

/**
 * Abstract controller class for data items. It must have composite by default, but it can
 * be overridden by composite params value. If composite params passed to constructor is null,
 * the composite for this controller is not important and if it's not created durind initialization
 * the exception will not be thrown. 
 * 
 * @author pavlov
 *
 */
public abstract class CommonItemController 
	extends CompositeController
	implements IItemController
{
	
	public enum EditMode { DIALOG, CONTAINER };

	private 	IPersistentManagerSession	mPersistentSession;
	private		List<RelatedController> 	mRelatedControllers;

	private		IDTO						mDTO;
	private		IDTOAccessChecker			mChecker;
	
	private		EditMode					editMode;
	
	public CommonItemController(ICompositeController parentController, Class<? extends ICommonObject> dataClass,
			CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
		
		mRelatedControllers = new ArrayList<RelatedController>();
		mPersistentSession = getSession().persistent();

		mChecker = createAccessChecker();
	}

	public CommonItemController(ICompositeController parentController, Class<? extends ICommonObject> dataClass,
			Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);

		mRelatedControllers = new ArrayList<RelatedController>();
		mPersistentSession = getSession().persistent();

		mChecker = createAccessChecker();
	}

	public void setEditMode(EditMode mode) {
		editMode = mode;
	}
	
	public EditMode getEditMode() {
		return editMode;
	}
	
	/**
	 * Метод создающий проверялку прав доступа.
	 * Можно переопределить на дочернем классе, если нужно
	 * обеспечить доступ к элементам иным чем созданные этим пользователем. 
	 */
	@Override
	public IDTOAccessChecker createAccessChecker() {
		return new AllowOwnChecker(getSession().getUser());		
	}

	@Override
	protected void doAfterCreateComposite() {
		if ((getComposite() != null) &&
			(getComposite().getControllerListener() != null) &&
			CommonItemEditControllerListener.class.isAssignableFrom((getComposite().getControllerListener().getClass()))
			) {
			try {
				((CommonItemEditControllerListener)getComposite().getControllerListener()).dataChangeFinished(null);
			} catch (DTOException e) {
				logger().error("Cannot update save button after composite was created.");
			}
		}
	}
	
	/**
	 * Получить DTO элемента данных.
	 * 
	 * @return
	 */
	@Override
	public IDTO getDTO() {
		return mDTO;
	}

	/**
	 * Метод создания DTO на контроллере. Если нужно создать какой-то другой тип объекта
	 * DTO, надо переопределить этот метод.
	 * 
	 * @param data элемент данных
	 * @return объект DTO
	 */
	protected CommonDTO createDTO(IModelObject data) throws DTOException {
		if (ICommonObject.class.isAssignableFrom(data.getClass()) &&
				((ICommonObject)data).getPersistenceManagerSession() == null) 
				((ICommonObject)data).setPersistentManagerSession(mPersistentSession);
		
		EditorDTO dto = new EditorDTO((IModelObject)data);
		dto.addAccessChecker(mChecker);
		return dto;
	}

	/**
	 * Метод открытия DTO на контроллере. Если нужно создать какой-то другой тип объекта
	 * DTO, надо переопределить этот метод.
	 * 
	 * @param data элемент данных
	 * @return объект DTO
	 */
	protected CommonDTO openDTO(IModelObject data) throws DTOException {
		if (ICommonObject.class.isAssignableFrom(data.getClass()) &&
			((ICommonObject)data).getPersistenceManagerSession() == null) 
			((ICommonObject)data).setPersistentManagerSession(mPersistentSession);

		EditorDTO dto = new EditorDTO((IModelObject)data);
		dto.addAccessChecker(mChecker);
		return dto;
	}

	/**
	 * Process data passed as usecase parameters.
	 */
	@Override
	public void processUsecaseParams() {
		// Get if the controller was run as usecase and has usecase params,
		// then get an ID of data item and open it. If there is no ID set (empty ID)
		// then create new DTO.
		if (getUsecaseParams() != null) {
			String id = (String)getUsecaseParams().get("id");
			logger().info("Passed ID of element {} in usecase params", id);
			try {
				if (id != null) {
					if (!"".equals(id)) {
						openById(id);
					} else {
						create();
					}
				}
			} catch (OpenException | DTOException ex) {
				ex.printStackTrace();
			}		
		}		
	}

	/**
	 * Создание нового объекта данных через контроллер.
	 * 
	 */
	@Override
	public void create() throws DTOException {
		setDTO(
				PersistentObjectFactory.getInstance().createEditorDTO(
						mDataClass, EditorDTO.class, mPersistentSession,
						new AllowOwnChecker(getSession().getUser())
				)
			);
		doAfterCreate();
	}

	public ICommonObject createDataElement() throws DTOException {
		if (mDataClass == null) {
			throw new DTOException("Класс данных ORM неизвестен, невозможно создать композит", null);
		}

		try {
			Constructor<?> ctor = mDataClass.getConstructor(IPersistentManagerSession.class);
			return (ICommonObject) ctor.newInstance(mPersistentSession);
		} catch (NoSuchMethodException ex) {
			throw new DTOException("В объекте ORM, возможно, не определен правильный конструктор!", ex);			
		} catch (SecurityException ex) {
			throw new DTOException("Ошибка создания элемента!", ex);			
		} catch (IllegalArgumentException ex) {
			throw new DTOException("Неверный аргумент конструктора!", ex);			
		} catch (InstantiationException ex) {
			throw new DTOException("Создание экземпляра неудачно!", ex);
		} catch (IllegalAccessException ex) {
			throw new DTOException("Проверьте видимость конструктора!", ex);
		} catch (InvocationTargetException ex) {
			throw new DTOException("Ошибка создания элемента!", ex);
		}		
	}
	
	/**
	 * Установить элемент DTO для этого контроллера, при этом не 
	 * вызывать refresh на этом контроллере и его субконтроллерах.
	 * 
	 * @param dto
	 * @throws DTOException
	 */
	public final void setDTONoRefresh(IDTO dto) throws DTOException {
		mDTO = dto;
		if (getComposite() != null)
			((CommonItemComposite)getComposite()).attachDTO(mDTO, false);		
	}
	
	/**
	 * Установить элемент DTO для этого контроллера, и 
	 * вызывать refresh на этом контроллере и его субконтроллерах.
	 * 
	 * @param dto
	 */
	@Override
	public void setDTO(IDTO dto)  throws DTOException {
		setDTONoRefresh(dto);
		
		refresh(true);
		
		// Обновить данные во всех связанных контроллерах
		for (RelatedController ctrl : mRelatedControllers) {
			ctrl.refresh(true);
		}
	}
	
	/**
	 * Открытие контроллера на элемент данных.
	 * 
	 * @param id
	 * @throws OpenException
	 */
	@Override
	public final void openById(String id) throws OpenException {	
		ICommonObject data = (ICommonObject) mPersistentSession.open(mDataClass, id);
		if (data != null) {
			open(data);
		} else {
			throw new OpenException(id, "No "+mDataClass.getSimpleName()+" with such ID found", null);
		}
	}

	/**
	 * Передача контроллеру в управление готового элемента данных.
	 * 
	 * @param data
	 */
	@Override
	public final void open(ICommonObject data) throws OpenException{		
		if (data != null) {
			try {
				// Сначала открываем объект(тут важен порядок действий), 			
				// потом приаттачиваем его к композиту 
				setDTO(openDTO(data));

				// И только потом вызываем обработчик, т.к. этот обработчик может
				// использовать данные композита - надо, чтобы DTO на композите уже был.
				doAfterOpen();
			} catch (DTOException ex) {				
				throw new OpenException(data.getId(), "Cannot get data of "+data.getClass().getSimpleName()+": "+ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void doAfterCreate() {}
	
	@Override
	public void doAfterOpen() throws OpenException {}
	
	@Override
	public void doBeforeSave() throws SaveException {}
	
	@Override
	public void doAfterSave() throws SaveException {}
	
	/**
	 * Обновить отображение данных на композите.
	 */
	@Override	
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		if (getComposite() != null)
			((CommonItemComposite)getComposite()).refresh();
		try {
			super.refresh(refreshSubcontrollers);
		} catch (Exception ex) {
			throw new DTOException("Exception on subcontrollers refresh", ex);
		}
	}
	
	/**
	 * Очистка контроллера.
	 */
	@Override
	public void close() {
		/*try {
			if (mData != null) {
				if (mData.getEditFlag())
					mData.close();
			}
		} catch (LockException ex) {
			handleError("Ошибка закрытия элемента.", ex);
		}*/
		super.close();
	}

	/**
	 * Сохранение элемента данных.
	 * 
	 * @throws SaveException
	 * @throws DTOException 
	 * @throws ValidationException 
	 */
	@Override
	public void save() throws SaveException, DTOException {
		if (getDTO() != null) {
			
			doBeforeSave();
			
			logger().debug("Сохранение данныех из связанных контроллеров, до сохранения объекта...");
			for (RelatedController ctrl : mRelatedControllers) {
				if (ctrl.getSaveMode() == SaveMode.SAVE_BEFORE)
					ctrl.save();
			}
			
			logger().debug("Object saving...");
			getDTO().save();

			logger().debug("Сохранение данныех из связанных контроллеров, после сохранения объекта...");
			for (RelatedController ctrl : mRelatedControllers) {
				if (ctrl.getSaveMode() == SaveMode.SAVE_AFTER)
					ctrl.save();
			}

			logger().debug("Object saved ID: {}", getDTO().getId());

			// Все что выполняется после сохранения
			doAfterSave();

			logger().debug("Saved everything!");
		} else {
			throw new SaveException("No data object to save!", null);
		}
	}

	@Override
	public void saveCommit() throws SaveException {
		if (getDTO() != null) {
			logger().debug("Committing transaction to store changes.");
			getDTO().saveCommit();
		}
	}

	/**
	 * При редактировании элемента данных на форме, они не
	 * сохраняются сразу в элемент данных, а записываются
	 * в буфер DTO.
	 * 
	 * Этот метод обнуляет изменения и перезаписывает данные в буфере
	 * данными из элмента данных.
	 * @throws DTOException 
	 */	
	@Override
	public void revertChanges() throws DTOException {
		getDTO().clearChangesBuffer();
		if (getComposite() != null)
			((CommonItemComposite)getComposite()).refresh();
	}

	/**
	 * Получить данные о том, есть ли изменения в буфере и отличаются ли
	 * они от оригинала.
	 * 
	 * @return
	 * @throws DTOException 
	 */
	@Override
	public ArrayList<String> getDataChanged() throws DTOException {
		if ((getDTO() != null) && (getDTO().getChangedData() != null)) {
			logger().info("getDataChanged: changed data is : {}", getDTO().getChangedData());
			return getDTO().getChangedData();
		} else {
			return null;
		}
	}
	
	/**
	 * Получить состояние, изменились ли данные в связанных контроллерах
	 * списков?
	 * 
	 * @return
	 */
	@Override
	public boolean getRelatedDataChanged() {
		for (RelatedController ctrl : mRelatedControllers) {
			if (ctrl.getChanged()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Валидация данных перед сохранением.
	 * Если валидация не прошла, то элемент не будет
	 * сохранен.
	 * 
	 * Переопределить в дочерних классах контроллеров.
	 * 
	 * @throws SaveException
	 */
	public void validate() throws ValidationException {}

	/**
	 * Нормализация данных перед сохранением,
	 * выполняет преобразование данных к нормальному виду.
	 * Если нормализация невозможна, то вызывает SaveException.
	 * 
	 * Переопределить в дочерних классах контроллеров.
	 * 
	 * @throws SaveException 
	 */
	public void normalize() {}
	
	/**
	 * Этот метод используется для изменения состояния кнопок сохранения
	 * элмента и вообще управлением состоянием элементов управления, которое
	 * связано с состоянием кнопки "Сохранить". Такая кнопка есть не во всех
	 * формах, поэтому метод нужно переопределять на всех контроллерах где он
	 * нужен.
	 *  
	 * @param enabled
	 */
	public void setSaveButtonEnabled(boolean enabled) {
		if (getComposite() != null) {
			// Если не заполнены обязательные поля - никогда не разрешать сохранение!
			boolean mandatoryFilled = ((CommonItemComposite)getComposite()).getIsMandatoryFilled(); 
			((CommonItemComposite)getComposite()).setSaveEnabled(enabled&&mandatoryFilled);
		}
	}

	/**
	 * Добавить контроллер связанного набора данных, то есть списка.
	 * 
	 * @param ctrl
	 */
	@Override
	public void addRelatedController(RelatedController ctrl) {
		mRelatedControllers.add(ctrl);
	}

	@Override
	public void uninit() {
		if ((getComposite() != null) && CommonItemComposite.class.isAssignableFrom(getComposite().getClass()))
			((CommonItemComposite)getComposite()).removeDTO();
		super.uninit();
	}
	
	/**
	 * Был ли элемент сохранен, есть ли у него ID?
	 * @return
	 * @throws DTOException 
	 */
	public boolean persisted() throws DTOException {
		if (getDTO() != null) {
			if (getDTO().getId() != null) return true;
			return false;
		} else {
			throw new DTOException("No DTO object here",  null);
		}
	}

	@Override
	public String getNavigationString() {
		String tag = getNavigationTag();

		// Add ID of data item to navigation string if it was passed
		// on usecase run.
		@SuppressWarnings("unchecked")
		Map<String, Object> params = (Map<String, Object>)params().get("usecaseParams");
		if ((params != null) && (params.get("id") != null)) {
			tag += "?id="+params.get("id");
		}
		
		return tag;
	}
}

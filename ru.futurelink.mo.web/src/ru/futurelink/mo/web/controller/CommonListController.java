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

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.composites.CommonDataComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListController;
import ru.futurelink.mo.web.exceptions.InitException;

abstract public class CommonListController 
	extends CompositeController
	implements IListController 
{
	private FilterDTO						mFilter;
	private CommonDTOList<? extends CommonDTO>	 mDTO;
	
	public CommonListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);	
	}

	public CommonListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	/**
	 * Разрешить или запретить использование элемента панели инструментов с названием toolName.
	 * 
	 * @param toolName
	 * @param enabled
	 */
	public void setToolEnabled(String toolName, boolean enabled) {
		if (getComposite() != null)
			((CommonDataComposite)getComposite()).setToolEnabled(toolName, enabled);
	}
	
	@Override
	public void init() throws InitException {
		// Создаем пустой объект фильтра
		if (mFilter == null)
			mFilter = new FilterDTO();		

		// Всегда создаем пустой список DTO для контроллера,
		// если надо, его всегда можно заменить своим списком.	
		try {
			setDTO(new EditorDTOList<CommonDTO>(getSession().persistent(), new AllowOwnChecker(getSession().getUser()), CommonDTO.class));
		} catch (DTOException e) {
			handleError("Ошибка инициализации контроллера при установке объекта DTO!", e);
		}

		super.init();
	}
	
	@Override
	public void uninit() {
		logger().debug("Очистка DTO списка");
		
		mDTO = null;
		mFilter = null;
		/*try {
			((CommonListComposite)getComposite()).refresh();
		} catch (DTOException e) {
			handleError("Ошибка деинициализации контроллера при уничтожении объекта DTO!", e);
		}*/

		super.uninit();
	}

	/**
	 * Установить объект DTO для использования в списке,
	 * если у контроллера есть еще композит, то этот объект
	 * приаттачивается еще и к нему.
	 * 
	 * Метод вызывать изнутри контроллера. Например в методе
	 * handleDataQuery().
	 *  
	 * @param dto
	 * @throws DTOException 
	 */
	protected synchronized void setDTO(CommonDTOList<? extends CommonDTO> dto) throws DTOException {
		removeDTO();	// Очищаем старый объект DTO
		
		mDTO = dto;
		
		// Обновляем список
		if (getComposite() != null)
			((CommonListComposite)getComposite()).refresh();
	}

	/**
	 * Получить список объектов DTO этого контроллера списка.
	 * 
	 * @return
	 * @throws DTOException 
	 */
	protected CommonDTOList<? extends CommonDTO> getDTO() throws DTOException {
		return mDTO;
	}
	
	/**
	 * Очистка от отвязка списка DTO от контроллера, и, если
	 * есть композит, то от композита. Не стоит использовать этот метод
	 * в одиночку, т.к. очень желательно, чтобы и у контроллера и у композита
	 * все таки был хотя бы пустой объект DTO.
	 * @throws DTOException 
	 * 
	 */
	protected synchronized void removeDTO() throws DTOException {
		if (mDTO != null) {
			mDTO.clear();
			mDTO = null;	// Удаляем DTO
		}

		// Обновляем композит
		if (getComposite() != null)
			((CommonListComposite)getComposite()).refresh();

		// Собираем мусор
		System.gc();
		
		logger().debug("DTO списка удален");		
	}

	/**
	 * Очистка DTO списка. Удаляем все данные из DTO и обновляем
	 * отображения списка.
	 * @throws DTOException 
	 */
	public synchronized void clearDTO() throws DTOException {
		// Уничтожаем содержимое списка в контроллере
		if (mDTO != null) {
			mDTO.clear();
			
			// Обновляем список
			if (getComposite() != null)
				((CommonListComposite)getComposite()).refresh();
		}

		// Собираем мусор
		System.gc();

		logger().debug("Очистка DTO списка прошла");
	}
	
	/**
	 * Выбрать в списке элемент данных.
	 * 
	 * @param data
	 */
	@Override
	public void setActiveData(CommonDTO data) {
		if (getComposite() != null)
			((CommonListComposite)getComposite()).setActiveData(data);
	}

	/**
	 * Получить выбранный в списке элемент данных.
	 * 
	 * @return объект DTO текущего элемента данных
	 */
	@Override
	public CommonDTO getActiveData() {
		if (getComposite() != null)
			return ((CommonListComposite)getComposite()).getActiveData();
		else return null;
	}

	/**
	 * Объект DTO фильтра.
	 * 
	 * @return
	 */
	public FilterDTO getFilter() {
		return mFilter;
	}

	/**
	 * Установить внешний объект DTO фильтра.
	 * 
	 * @param filter
	 */
	public void setFilter(FilterDTO filter) {
		mFilter = filter;
	}
	
	/**
	 * Создание контроллера элемента.
	 * 
	 * @param parentComposite
	 * @return
	 */
	public CommonItemController createItemController(
			ICompositeController parentController, 
			Composite parentComposite, 
			CompositeParams params) {
		Class<?> itemControllerClass = (Class<?>) params().get("itemControllerClass");
		if (itemControllerClass != null) {			
			Constructor<?> constr;
			CommonItemController result = null;
			try {
				if (parentComposite == null) {
					constr = itemControllerClass.getConstructor(
							ICompositeController.class,
							Class.class, 
							CompositeParams.class);

					result = (CommonItemController) constr.newInstance(parentController, getDataClass(), params);					
				} else {
					constr = itemControllerClass.getConstructor(
						ICompositeController.class, 
						Class.class, 
						Composite.class, 
						CompositeParams.class);
					result = (CommonItemController) constr.newInstance(parentController, getDataClass(), parentComposite, params);
				}
			} catch (Exception ex) {
				handleError("Ошибка при создании контроллера элемента данных. Проверьте конструктор.", ex);
				ex.printStackTrace();
				result = null;
			}
			return result;
		} else {
			handleError("В контроллере списка не задан параметр itemControllerClass.", null);
			return null;
		}
	}

	@Override
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		logger().debug("Called refresh() on CommonListController, requesting data...");
		handleDataQuery();
		try {
			super.refresh(refreshSubcontrollers);
		} catch (Exception e) {
			throw new DTOException("Ошибка обноелния данных", e);
		}
	}
	
	@Override
	public void processUsecaseParams() {}
}

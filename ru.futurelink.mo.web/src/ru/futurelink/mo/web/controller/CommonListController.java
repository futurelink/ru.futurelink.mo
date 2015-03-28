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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.types.DateRange;
import ru.futurelink.mo.web.composites.CommonDataComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListController;
import ru.futurelink.mo.web.exceptions.InitException;

abstract public class CommonListController 
	extends CompositeController
	implements IListController 
{
	private FilterDTO						filterDTO;
	private CommonDTOList<? extends IDTO>	dto;
	private IDTOAccessChecker				accessChecker;

	public CommonListController(ICompositeController parentController,
			Class<? extends ICommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
		
		setAccessChecker(createAccessChecker());
	}

	public CommonListController(ICompositeController parentController,
			Class<? extends ICommonObject> dataClass, Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
		
		setAccessChecker(createAccessChecker());
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
	public IDTOAccessChecker getAccessChecker() {
		return accessChecker;
	}
	
	@Override	
	public void setAccessChecker(IDTOAccessChecker accessChecker) {
		this.accessChecker = accessChecker;
	}
	
	@Override
	public void init() throws InitException {
		// Создаем пустой объект фильтра
		if (filterDTO == null)
			filterDTO = new FilterDTO();		

		// Всегда создаем пустой список DTO для контроллера,
		// если надо, его всегда можно заменить своим списком.	
		try {
			setDTO(
				new EditorDTOList<CommonDTO>(
						getSession().persistent(), getAccessChecker(), CommonDTO.class
				)
			);
		} catch (DTOException e) {
			handleError("Ошибка инициализации контроллера при установке объекта DTO!", e);
		}

		super.init();
	}
	
	@Override
	public void uninit() {
		logger().debug("Очистка DTO списка");
		
		dto = null;
		filterDTO = null;
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
	protected synchronized void setDTO(CommonDTOList<? extends IDTO> dto) 
			throws DTOException {
		removeDTO();	// Очищаем старый объект DTO
		
		this.dto = dto;

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
	@Override
	public CommonDTOList<? extends IDTO> getDTO() throws DTOException {
		return dto;
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
		if (dto != null) {
			dto.clear();
			dto = null;	// Удаляем DTO
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
	 * 
	 * @throws DTOException 
	 */
	public synchronized void clearDTO() throws DTOException {
		// Уничтожаем содержимое списка в контроллере
		if (dto != null) {
			dto.clear();
			
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
	public void setActiveData(IDTO data) {
		if (getComposite() != null)
			((CommonListComposite)getComposite()).setActiveData(data);
	}

	/**
	 * Получить выбранный в списке элемент данных.
	 * 
	 * @return объект DTO текущего элемента данных
	 */
	@Override
	public IDTO getActiveData() {
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
		return filterDTO;
	}

	/**
	 * Установить внешний объект DTO фильтра.
	 * 
	 * @param filter
	 */
	public void setFilter(FilterDTO filter) {
		filterDTO = filter;
	}
	
	public final Predicate getFilterConditions(
			CriteriaQuery<? extends ModelObject> cq,			
			Root<? extends ModelObject> root,
			Map<String, Object> parametres) {

		Predicate pre = null;
		CriteriaBuilder cb = getSession().persistent().getEm().getCriteriaBuilder();
		Map<String, List<Object>> conditions = filterDTO.getConditions();

		if (!conditions.isEmpty()) {
			int k = 0;
			for (String fieldName : conditions.keySet()) {
				k++;
				for (int n = 0; n < conditions.get(fieldName).size(); n++) {
					Predicate newPre = null;
					// Empty conditions are skipped
					if (conditions.get(fieldName).get(n) != null) {

						// If condition is a CommonDTO object - use data item specific value processing
						if (CommonDTO.class.isAssignableFrom(conditions.get(fieldName).get(n).getClass())) {
							try {
								String id = ((CommonDTO)conditions.get(fieldName).get(n)).getId();
								newPre = cb.equal(
										root.get(fieldName).get("id"), 
										cb.parameter(ModelObject.class, "fieldData" + k + n));

								// Put return parameter
								parametres.put("fieldData" + k + n, id);
							} catch (DTOException ex) {
								// TODO handle this error
								ex.printStackTrace();
							}
						} else {
							// If condition is a string or any other value - use simple string processing
							if (!conditions.get(fieldName).get(n).equals("")) {
								// Обработка диапазона дат "от" и "до" 
								if (DateRange.class.isAssignableFrom(conditions.get(fieldName).get(n).getClass())) {
									newPre = cb.and(
										cb.greaterThanOrEqualTo(
											root.<Date>get(fieldName), 
											cb.parameter(Date.class, "fieldData" + k + n + "min")),
										cb.lessThanOrEqualTo(
											root.<Date>get(fieldName), 
											cb.parameter(Date.class, "fieldData" + k + n + "max"))
									);

									// Put return parameter
									parametres.put("fieldData" + k + n + "min", 
										((DateRange)conditions.get(fieldName).get(n)).getBeginDate());						
									parametres.put("fieldData" + k + n + "max", 
										((DateRange)conditions.get(fieldName).get(n)).getEndDate());						
								} else {					
									newPre = cb.equal(
											root.get(fieldName), 
											cb.parameter(Object.class, "fieldData" + k + n));

									// Put return parameter
									parametres.put("fieldData" + k + n, conditions.get(fieldName).get(n));
								}
							}
						}

						// Add filter predicate as 'AND'/'OR' clause
						if (newPre != null)
							if (pre == null)
								pre = newPre;
							else {
								// If there is more than one condition on the same field, use "or"
								if (n < conditions.get(fieldName).size()-1)
									pre = cb.or(pre, newPre);
								else
									pre = cb.and(pre, newPre);
							}

					}
				}
			}
		}

		cb = null;
		
		return pre;
	}

	public final void applyFilterConditionsParameters(Query query, Map<String, Object> parametres) {
		if ((query != null) && (parametres != null)) {
			for (String key : parametres.keySet()) {
				query.setParameter(key, parametres.get(key));
			}
		}
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

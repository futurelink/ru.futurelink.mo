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

import javax.persistence.Query;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.access.AllowAllChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public abstract class RelatedItemController 
	extends CommonItemController 
	implements RelatedController {

	private CommonItemController	mRelatedController;
	
	private String mFieldName;
	private String mFieldGetterName;
	private String mFieldSetterName;
	
	// Направление формирования связи
	public static enum Direction { FORWARD, BACKWARD };
	
	private Direction mDirection;

	/**
	 * @param parentController
	 * @param dataClass
	 * @param container
	 * @param compositeParams
	 */
	public RelatedItemController(ICompositeController parentController,
			Class<? extends ICommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
		
		setRelatedController((CommonItemController) parentController);
		
		mDirection = Direction.FORWARD;
	}

	/**
	 * Установить контроллер, к которому привязан этот контроллер.
	 * 
	 * @param ctrl объект основного контроллера
	 */
	private final void setRelatedController(CommonItemController ctrl) {
		mRelatedController = ctrl;			// Делаем ссылку на контроллер связи,		
		ctrl.addRelatedController(this);	// и даем ему ссылку на себя.
	}

	public final void setDataField(String fieldName, String fieldGetter,
			String fieldSetter) {
		mFieldName = fieldName;
		mFieldGetterName = fieldGetter;
		mFieldSetterName = fieldSetter;
	}

	public void setDirection(Direction direction) {
		mDirection = direction;
	}
	
	@Override
	protected void doAfterInit() throws InitException {
	}
	
	@Override
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		boolean isNew = false;
		CommonDTO object = null;
		if (mDirection == Direction.FORWARD) {
			// Если направление связки прямое, то есть в родительском элементе данных
			// есть ссылка на связанный объект, то берем из родительского.
			object = (CommonDTO) mRelatedController.getDTO().getDataField(
				mFieldName, 
				mFieldGetterName, 
				mFieldSetterName
			);
			if (object != null) object.addAccessChecker(new AllowAllChecker());
		} else if (mDirection == Direction.BACKWARD) {
			
			if (mFieldName == null) {
				throw new DTOException("Не указано поле связи. Надо вызывать setDataField на этом контроллере, чтобы его указать!", null);
			}
			
			// Если направление связки обратное, то есть в этом элементе данных есть
			// ссылка на родительский объект, то получаем объект связанный.
			if (mRelatedController.getDTO().getId() != null) { 
				Query q = getSession().persistent().getEm().createQuery(
						"select d from "+mDataClass.getSimpleName()+" d where d."+mFieldName+".id = :relatedItemId"
					);
				q.setParameter("relatedItemId", mRelatedController.getDTO().getId());
				//q.setParameter("creator", getSession().getDatabaseUser());
				if (!q.getResultList().isEmpty()) {
					ICommonObject dataItem = (ICommonObject) q.getResultList().get(0);
					dataItem.setPersistentManagerSession(getSession().persistent());
					object = new EditorDTO(dataItem);
					object.addAccessChecker(new AllowAllChecker());
				}
			}
			
			/*
			 * Если объект родителя не сохранен или если не найден соотстветсвующий объект
			 * в базе, то надо создать новый.
			 */
			if (object == null) {
				// Cоздаем новую DTO для связки, пустую.
				try {
					Constructor<? extends ICommonObject> constr = mDataClass.getConstructor(PersistentManagerSession.class);
					ICommonObject dataItem = constr.newInstance(getSession().persistent());
					
					object = new EditorDTO(dataItem);
					object.addAccessChecker(new AllowAllChecker());
					
					// Установим связку сразу
					object.setDataField(mFieldName, mFieldGetterName, mFieldSetterName, mRelatedController.getDTO());
					
					isNew = true;
				} catch (NoSuchMethodException | 
						SecurityException | 
						InstantiationException | 
						IllegalAccessException | 
						IllegalArgumentException | 
						InvocationTargetException ex) {
					throw new DTOException("Ошибка создания нового связанного элемента", ex);
				}									
			}
		}

		setDTONoRefresh(object);

		try {
			if (isNew)
				doAfterCreate();
			else
				doAfterOpen();
		} catch (OpenException ex) {
			throw new DTOException("Ошибка после открытия элемента!", ex);
		}
		
		super.refresh(refreshSubcontrollers);
	}

	/**
	 * При установке DTO нужно передать объект выщестоящему контроллеру, с которым
	 * мы связаны.
	 */
	@Override
	public void setDTO(CommonDTO dto) throws DTOException {
		mRelatedController.getDTO().setDataField(
				mFieldName, 
				mFieldGetterName, 
				mFieldSetterName, 
				dto
			);

		super.setDTO(dto);
	}
	
	@Override
	public SaveMode getSaveMode() {
		if (mDirection == Direction.FORWARD) {
			return SaveMode.SAVE_BEFORE;
		} else {
			return SaveMode.SAVE_AFTER;
		}
	}
}

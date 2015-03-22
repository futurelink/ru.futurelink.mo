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

package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.ICompositeController;

abstract public class CommonField implements IField {
	protected Control 		control;
	protected String		dataFieldName;
	protected String		dataFieldSetter;
	protected String		dataFieldGetter;
	protected CommonItemControllerListener parentControllerListener;
	private	   CommonItemComposite		dataComposite;
	private	   IDTO						dto;
	protected ModifyListener			fieldModifyListener;
	private	   boolean					mandatoryFlag;
	private	   boolean					useOnlyOneCondition = true;
	protected Composite					parent;
	private ApplicationSession			session;
	
	private ICompositeController		parentController;
		
	public CommonField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonItemComposite dataComposite) {		
		this.parent = parent;
		this.dataComposite = dataComposite;
		this.session = session;
	}

	public CommonField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonDTO dto) {
		this.parent = parent;		
		this.dto = dto;
		this.session = session;
	}

	public void setParentController(ICompositeController controller) {
		parentController = controller;
	}
	
	public ICompositeController getParentController() {
		return parentController;
	}
	
	public Composite getParentComposite() {
		if (parentController != null)
			return parentController.getComposite();
		return parent;
	}
	
	public void addModifyListener(ModifyListener listener) {
		fieldModifyListener = listener;
	}

	public void setDataField(String dataField, String dataFieldGetter, String dataFieldSetter) {		
		dataFieldName = dataField;
		this.dataFieldGetter = dataFieldGetter;
		this.dataFieldSetter = dataFieldSetter;
	}

	/**
	 * Метод возвращает обработчик, привязанный к композиту элемента
	 * данных (CommonItemComposite).
	 */
	@Override
	public CommonControllerListener getControllerListener() {
		if (dataComposite != null)
			return dataComposite.getControllerListener();
		return null;
	}
	
	/**
	 * Установить полю ссылку на обработчик роительского контроллера.
	 */
	public void setParentControllerListener(CommonItemControllerListener listener) {
		parentControllerListener = listener;
	}

	/**
	 * Обработать обязательность поля. Показать как-либо маркер, свойственный данному полю.
	 */
	abstract public void handleMandatory();
	
	/**
	 * Получить элемент DTO для поля:
	 * - если используется в режиме с DataComposite - возвращается элемент DTO из окна отображения,
	 * - если используется с конструктором в который передан DTO - возвращается сохраненный DTO.
	 * @return
	 */
	@Override
	public IDTO getDTO() {
		if ((dataComposite == null) || (dto != null)) return dto;
		if (dataComposite.getDTO() != null) {
			return dataComposite.getDTO();
		} else {
			return null;
		}
	}
	
	/**
	 * Установить объект DTO для работы с полем.
	 * 
	 * @param dto
	 */
	@Override
	public void setDTO(IDTO dto) {
		this.dto = dto;
	}
	
	/**
	 * Получить объект самого себя. Используется в реализациях некоторых обработчиков.
	 * 
	 * @return
	 */
	public final CommonField getSelf() {
		return this;
	}
	
	/**
	 * Очистить поле до состояния по-умолчанию.
	 */
	@Override
	public void clear() {}

	/**
	 * Установить признак обязательности поля.
	 *
	 * @param isMandatory
	 * @throws DTOException 
	 */
	@Override
	public final void setMandatory(boolean isMandatory) {
		mandatoryFlag = isMandatory;
	}
	
	/**
	 * Получить признак обязательности поля.
	 * 
	 * @return
	 */
	@Override
	public final boolean getMandatory() {
		return mandatoryFlag;
	}

	@Override
	public void setEditable(boolean isEditable) {
		control.setEnabled(isEditable);
	}

	@Override
	public boolean getEditable() {
		return control.getEnabled();
	}
	
	@Override
	public String getDataFieldName() {
		return dataFieldName;
	}

	@Override
	public String getDataFieldGetter() {
		return dataFieldGetter;
	}

	@Override
	public String getDataFieldSetter() {
		return dataFieldSetter;
	}
		
	public boolean getUseOnlyOneCondition() {
		return useOnlyOneCondition;
	}
	
	public void setUseOnlyOneCondition(boolean useOnlyOneCondition) {
		this.useOnlyOneCondition = useOnlyOneCondition;
	}

	public void setFont(Font font) {
		control.setFont(font);
	}
	
	public Control getControl() {
		return control;
	}
	
	public void setFocus() {
		control.setFocus();
	}

	public void setEnabled(boolean enabled) {
		control.setEnabled(enabled);
	}
	
	public void setLayoutData(Object layoutData) {
		control.setLayoutData(layoutData);
	}
	
	public ApplicationSession getSession() {
		if (parentController != null)
			return parentController.getSession();
		return session;
	}
}

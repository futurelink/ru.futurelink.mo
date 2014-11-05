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

package ru.futurelink.mo.web.composites.fields.datapicker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.composites.toolbar.ItemToolbar;
import ru.futurelink.mo.web.composites.toolbar.ToolbarListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;

abstract public class CommonDataPickerComposite extends CommonListComposite {
	private static final long serialVersionUID = 1L;

	public CommonDataPickerComposite(ApplicationSession session, Composite parent, int style,
			Class<?> tableClass, CompositeParams params) {
		super(session, parent, style, params);
	}

	@Override
	protected CommonToolbar createToolbar() {
		
		/*
		 * Переносим нужные параметры на тулбар
		 */
		CompositeParams params = new CompositeParams();
		params.add("allowCreate", getParam("allowCreate"));
		params.add("itemControllerClass", getParam("itemControllerClass"));
		
		/*
		 * Создаем недостаюзщие кнопочки в зависимости от параметров тулбара.
		 */
		ItemToolbar toolbar = new CommonDataPickerToolbar(getSession(), this, SWT.NONE, params);
		toolbar.addToolBarListener(new ToolbarListener() {			
			@Override
			public void toolBarButtonPressed(Button button) {
				String buttonName = button.getData().toString();
				try {
					if (buttonName.equals("save") ) {
						setOwnerDialogResult(buttonName);
						((DataPickerControllerListener)getControllerListener()).ok();				
					} else  if (buttonName.equals("close")) {
						setOwnerDialogResult(buttonName);
						((DataPickerControllerListener)getControllerListener()).cancel();					
					} else  if (buttonName.equals("create")) {
						((DataPickerControllerListener)getControllerListener()).create();					
					} else  if (buttonName.equals("edit")) {
						((DataPickerControllerListener)getControllerListener()).edit();					
					}
				} catch (DTOException | InitException ex) {
					getControllerListener().sendError("Ошибка операции", ex);
				}
				
			}
		});
		return toolbar;
	}

	@Override
	public void selectById(String id) {
		
	}

	@Override
	public void selectByDTO(CommonDTO dto) {
		
	}

}

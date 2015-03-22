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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

public class CheckBoxField extends CommonField {
	public CheckBoxField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);
		
		createField();
	}

	public CheckBoxField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonDTO dto) {
		super(session, parent, style, params, dto);
		
		createField();
	}

	private void createField() {
		control = new Button(parent, SWT.CHECK);
		((Button)control).addSelectionListener(new SelectionListener() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					if (EditorDTO.class.isAssignableFrom(getDTO().getClass())) {
						getDTO().setDataField(dataFieldName, dataFieldGetter, dataFieldSetter, ((Button)control).getSelection());
					}
					
					if (getControllerListener() != null) {
						((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());
						((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
					}
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления элмента чекбокса!", ex);
				} 
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	}
	
	public void setText(String text) {
		((Button)control).setText(text);
	}

	@Override
	public void refresh() throws DTOException {
		boolean selected = false;
		if (getDTO().getDataField(dataFieldName, dataFieldGetter, dataFieldSetter) != null) {
			selected = (Boolean)getDTO().getDataField(dataFieldName, dataFieldGetter, dataFieldSetter);
		}
		((Button)control).setSelection(selected);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void handleMandatory() {}

	@Override
	public Object getValue() {
		return ((Button)control).getSelection();
	}

}

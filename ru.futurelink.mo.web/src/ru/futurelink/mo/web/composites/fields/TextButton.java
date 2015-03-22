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
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * The push button with text label retrieved from DTO object.
 * 
 * @author pavlov
 *
 */
public class TextButton extends CommonField {

	private SelectionListener mSelectionListener;
	
	public TextButton(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);

		control = new Button(parent, SWT.PUSH);
		((Button)control).addSelectionListener(new SelectionListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (mSelectionListener != null) {
					mSelectionListener.widgetSelected(arg0);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				if (mSelectionListener != null) {
					mSelectionListener.widgetDefaultSelected(arg0);
				}				
			}
		});
	}

	public String getText() {
		return ((Text)control).getText();
	}
	
	public void setText(String text) throws DTOException {
		if (!((Button)control).getText().equals(text)) {
			((Button)control).setText(text);
		}
	}

	@Override
	public void refresh() throws DTOException {
		Object f = getDTO().getDataField(dataFieldName, dataFieldGetter, dataFieldSetter);
		setText(f != null ? f.toString() : "");
	}

	public void addSelectionListener(SelectionListener listener) {
		mSelectionListener = listener;
	}

	@Override
	public boolean isEmpty() {
		return (getDTO() == null);
	}

	@Override
	public void handleMandatory() {
	
	}

	@Override
	public Object getValue() {
		return getDTO();
	}

}

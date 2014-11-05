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

package ru.futurelink.mo.web.topmenu;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

public class TopMenuButton extends CommonComposite {
	private static final long serialVersionUID = 1L;

	private Button mButton;

	private SelectionListener	mSelectionListener;

	public TopMenuButton(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);

		setData(RWT.CUSTOM_VARIANT, "topMenuElement");
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		l.marginTop = 0;
		l.marginBottom = 0;
		l.marginLeft = 0;
		l.marginRight = 0;
		l.marginHeight = 0;
		l.marginWidth = 0;
		setLayout(l);
		
		mButton = new Button(this, SWT.PUSH);
		mButton.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));
		mButton.addSelectionListener(new SelectionListener() {
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

	public void addSelectionListener(SelectionListener listener) {
		mSelectionListener = listener;
	}

	public void setText(String text) {
		mButton.setText(text);
	}
}


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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.futurelink.mo.web.app.ApplicationController;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.register.ApplicationConfig;

public class TopMenuComposite extends CommonComposite {

	protected 	ApplicationController		mApplicationController;
	protected	Label 						mLogoLbl;

	public TopMenuComposite(ApplicationSession session, Composite container,
			int style, CompositeParams params) {
		super(session, container, style, params);

		String systemMode = System.getProperty("ru.futurelink.mo.systemmode");

		// Создадим горизонтальное меню, которое к тому же будет еще контейнером
		// различных фенечек.
		setData(RWT.CUSTOM_VARIANT, "topMenu");
		//setBackground(new Color(getDisplay(), 255, 255, 255));

		GridLayout l = new GridLayout();
		l.numColumns = 0;
		l.marginWidth = 0;
		l.marginHeight = 24;
		l.marginLeft = 32;
		l.marginRight = 32;
		l.horizontalSpacing = 16;
		setLayout(l);

		((GridLayout)getLayout()).numColumns++;
		Image logo = new Image(getDisplay(), ApplicationConfig.class.getResourceAsStream("/images/fluvio_logo.png"));
		mLogoLbl = new Label(this, SWT.NONE);
		mLogoLbl.setData(RWT.CUSTOM_VARIANT, "topMenuElement");
		mLogoLbl.setImage(logo);	
		mLogoLbl.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL));

		if ((systemMode != null) && (systemMode.equals("test"))) {
			((GridLayout)getLayout()).numColumns++;
			Label systemModeLbl = new Label(this, SWT.NONE);
			systemModeLbl.setText("This is a testing system!");
			systemModeLbl.setBackground(new Color(getDisplay(), 255, 0, 0));
			systemModeLbl.setForeground(new Color(getDisplay(), 255, 255, 255));
		}
	}
	
	public void setTitle(String title) {}

	private static final long serialVersionUID = 1L;

}

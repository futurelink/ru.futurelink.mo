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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class TopMenuUserInfo extends CommonComposite {

	private static final long serialVersionUID = 1L;

	/**
	 * @param session
	 * @param container
	 * @param style
	 * @param params
	 */
	public TopMenuUserInfo(ApplicationSession session, Composite container,
			int style, CompositeParams params) {
		super(session, container, style, params);

		setLayout(new GridLayout());
		
		setData(RWT.CUSTOM_VARIANT, "topMenuUserInfo");
	}
}

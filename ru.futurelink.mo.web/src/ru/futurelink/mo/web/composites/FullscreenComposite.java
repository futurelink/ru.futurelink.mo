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

package ru.futurelink.mo.web.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Fullscreen composite to use as main application window composite.
 */
public class FullscreenComposite extends CommonComposite {

	private static final long serialVersionUID = 1L;

	public FullscreenComposite(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);
		
	    FillLayout layout = new FillLayout();
	    layout.type = SWT.VERTICAL;
	    setLayout(layout);
	}

}

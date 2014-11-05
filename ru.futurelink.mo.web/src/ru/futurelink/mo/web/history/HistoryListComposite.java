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

package ru.futurelink.mo.web.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @since 1.2
 */
public class HistoryListComposite extends SimpleListComposite {

	private static final long serialVersionUID = 1L;

	public HistoryListComposite(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style | SWT.BORDER, params);
	}

	@Override
	protected CommonToolbar createToolbar() {
		return null;
	}
	
}

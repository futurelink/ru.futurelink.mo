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

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;

public class HistoryToolbar extends CommonToolbar {

	private static final long serialVersionUID = 1L;

	public HistoryToolbar(ApplicationSession session, Composite parent,
			int style) {
		super(session, parent, style);
	}

}

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

package ru.futurelink.mo.web.controller;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface RelatedController {
	enum SaveMode { SAVE_AFTER, SAVE_BEFORE };

	public void		refresh(boolean refreshSubControllers) throws DTOException;
	public void		save() throws DTOException, SaveException;
	public boolean		getChanged();
	public SaveMode		getSaveMode();
}

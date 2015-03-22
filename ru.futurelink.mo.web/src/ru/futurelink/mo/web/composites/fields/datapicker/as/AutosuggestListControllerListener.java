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

package ru.futurelink.mo.web.composites.fields.datapicker.as;

import ru.futurelink.mo.web.controller.CommonListControllerListener;

/**
 * @author pavlov
 *
 */
public interface AutosuggestListControllerListener extends
		CommonListControllerListener {
	public void searchTextModified(String text);
}

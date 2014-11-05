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

package ru.futurelink.mo.web.app;

import java.util.Map;

import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;

/**
 * Application controller listener interface.
 *
 * This listener is to use with ApplicationController as controller listener.
 * 
 * @author pavlov
 *
 */
public interface ApplicationControllerListener extends CommonControllerListener {
	public CompositeController runUsecase(String usecaseName, Class<?> dataClass);
	public void refresh() throws Exception;
	public void navigate(String tag, Map<String, Object> params);
}

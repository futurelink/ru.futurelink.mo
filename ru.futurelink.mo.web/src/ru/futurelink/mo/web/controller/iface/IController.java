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

package ru.futurelink.mo.web.controller.iface;

import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public interface IController {
	
	/* Начальная обработка */

	public void init() throws InitException;
	public void uninit();

	/* Методы для работы с субконтроллерами */
	
	public void 		addSubController(IController controller) throws InitException;
	public void 		removeSubController(int index);
	public IController getSubController(int index);
	public int 		getSubControllerCount();

	/* Обработка сообщений */

	public void addControllerListener(CommonControllerListener listener);
	public CommonControllerListener getControllerListener();

	public void handleError(String errorText, Exception exception);

	public void refresh(boolean refreshSubcontrollers) throws Exception;

	/**
	 * @return
	 */
	public boolean getInitialized();
}

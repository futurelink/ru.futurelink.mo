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

import java.util.Locale;
import java.util.ResourceBundle;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public interface IComposite {
	public void init() throws InitException;
	
	public void addControllerListener(CommonControllerListener listener);
	public CommonControllerListener getControllerListener();
	
	public void setOwnerDialog(CommonDialog dialog);
		
	public void setStrings(ResourceBundle bundle);
	public Locale getLocale();
	public String getLocaleString(String stringName);
	public String getLocaleNumeric(Integer value, String single, String multiple);
	public String getErrorString(String stringName);
	
	public ApplicationSession getSession();	
}

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

import java.util.HashMap;

public class CompositeParams {
	
	private HashMap<String, Object> mParams;
	
	public CompositeParams() {
		mParams = new HashMap<String, Object>();
	}
	
	public CompositeParams add(String paramName, Object paramValue) {
		mParams.put(paramName, paramValue);
		return this;
	}
	
	public Object get(String paramName) {
		return mParams.get(paramName);
	}
}

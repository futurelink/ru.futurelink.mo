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

package ru.futurelink.mo.web.export;

import java.io.InputStream;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.iface.IUser;
import ru.futurelink.mo.web.controller.CommonControllerListener;

/**
 * @author pavlov
 *
 */
public abstract class ExportView {
	
	private CommonDTOList<? extends CommonDTO> 	mList;
	private CommonControllerListener				mControllerListener;
	private IUser									mUser;

	public ExportView(IUser user) {
		mUser = user;
	}
	
	final public IUser getUser() {
		return mUser;
	}
	
	final public void setDTO(CommonDTOList<? extends CommonDTO> dto) {
		mList = dto;
	}

	final public CommonDTOList<? extends CommonDTO> getDTO() {
		return mList;
	}

	/**
	 * Set report export controller listener for report view.
	 * 
	 * @param listener
	 */
	final public void addControllerListener(CommonControllerListener listener) {
		mControllerListener = listener;
	}

	/**
	 * Get report controller listener from report view.
	 * 
	 * @return
	 */
	final public CommonControllerListener getControllerListener() {
		return mControllerListener;
	}
	
	public String getMimeType() {
		return "text/html; charset=utf-8";
	}

	public String getContentDisposition() {
		return "filename=\"report.html\"";
	}
	
	/**
	 * Get contents of a report.
	 * 
	 * @return
	 */
	public abstract InputStream getContents();
}

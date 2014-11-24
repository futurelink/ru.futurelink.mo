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

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.CommonController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public abstract class ExportController extends CommonController {
	private ExportView								mView;
	private CommonDTOList<? extends CommonDTO>		mList;

	
	/**
	 * @param session
	 * @param dataClass
	 */
	public ExportController(ApplicationSession session,
			Class<? extends ICommonObject> dataClass) {
		super(session, dataClass);
	}

	public void setDTO(CommonDTOList<? extends CommonDTO> listDTO) {
		mList = listDTO;
	}

	public CommonDTOList<? extends CommonDTO> getDTO() {
		return mList;
	}
	
	public abstract ExportView createView();
	
	public void export() {			
		HttpSession session = RWT.getUISession().getHttpSession();
		session.setAttribute("exportController", getSelf());

		URL url = null;
		try {
			url = new URL(
					RWT.getRequest().getScheme(),
					RWT.getRequest().getServerName(),
					RWT.getRequest().getServerPort(),
					"/export"
				);
		} catch (MalformedURLException e) {
			handleError("Неверный адрес экспорта.", e);
			return;
		}

		getView().setDTO(getDTO());

		UrlLauncher launcher = RWT.getClient().getService( UrlLauncher.class );
		launcher.openURL(url.toString());	
	}

	public ExportView getView() {
		return mView;
	}
	
	@Override	
	public void handleError(String errorText, Exception exception) {
		
	}

	@Override
	public synchronized void init() throws InitException {
		mView = createView();
		
		super.init();
	}
	
	@Override
	protected void doBeforeInit() throws InitException {
		
	}

	@Override
	protected void doAfterInit() throws InitException {
		
	}

}

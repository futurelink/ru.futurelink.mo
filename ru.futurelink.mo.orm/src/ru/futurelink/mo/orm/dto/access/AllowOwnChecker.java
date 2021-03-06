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

package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * Агент проверки. Разрешает доступ только к своим обьектам, если владелец-создатель
 * объекта не равен пользователю системы - то права не даются.
 * 
 * Объекты с пустым создателем считаются доступными всем, права на них даются всегда.
 * 
 * @author pavlov
 *
 */
public class AllowOwnChecker implements IDTOAccessChecker {

	private IUser mUser;
	
	public AllowOwnChecker(IUser user) {
		mUser = user;
	}
	
	public IUser getUser() { return mUser; }
	
	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {
		if ("admin".equals(mUser.getLogin())) return true;

		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {
		if ("admin".equals(mUser.getLogin())) return true;

		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkSave(CommonDTO dto) {
		if ("admin".equals(mUser.getLogin())) return true;

		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkCreate(CommonDTO dto) {
		if ("admin".equals(mUser.getLogin())) return true;

		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

}

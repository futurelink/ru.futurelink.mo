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

import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.iface.ISessionUserAccess;

/**
 * @author pavlov
 *
 */
public class AllowOwnAndAccessedChecker implements IDTOAccessChecker {

	private ISessionUserAccess userAccess;
	
	/**
	 * 
	 */
	public AllowOwnAndAccessedChecker(ISessionUserAccess userAccess) {
		this.userAccess = userAccess;
	}
	
	@Override
	public boolean checkCreate(IDTO dto) {
		return checkOwnerAndAccessed(dto);
	}

	@Override
	public boolean checkRead(IDTO dto, String fieldName) {
		return checkOwnerAndAccessed(dto);
	}

	@Override
	public boolean checkWrite(IDTO dto, String fieldName) {
		return checkOwnerAndAccessed(dto);
	}

	@Override
	public boolean checkSave(IDTO dto) {
		return checkOwnerAndAccessed(dto);
	}

	private boolean checkOwnerAndAccessed(IDTO dto) {
		if ((dto != null) && (dto.getOwner() != null)) {
			// If there is user in session, check it
			if ((userAccess.getUser() != null) && 
				dto.getOwner().getId().equals(userAccess.getUser().getId())) return true;

			// If there is database access user in session, check it
			if ((userAccess.getUser() != null) && 
					dto.getOwner().getId().equals(userAccess.getDatabaseUser().getId())) return true;

			return false;
		}
		
		// For objects that have no owner - allow all
		return true;
	}
}

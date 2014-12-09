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
import ru.futurelink.mo.orm.dto.EditorDTOWithLinkage;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * @author pavlov
 *
 */
public class AllowOwnAndLinkedChecker extends AllowOwnChecker {

	public AllowOwnAndLinkedChecker(IUser user) {
		super(user);
	}
	
	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {

		if (dto != null && 	EditorDTOWithLinkage.class.isAssignableFrom(dto.getClass())) {			
			if (((EditorDTOWithLinkage)dto).getLinkageCreator().getId().equals(getUser().getId())) return true;
		}
		
		return super.checkRead(dto, fieldName);
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {

		if (dto != null && 	EditorDTOWithLinkage.class.isAssignableFrom(dto.getClass())) {			
			if (((EditorDTOWithLinkage)dto).getLinkageCreator().getId().equals(getUser().getId())) return true;
		}

		return super.checkWrite(dto, fieldName);
	}
	
}

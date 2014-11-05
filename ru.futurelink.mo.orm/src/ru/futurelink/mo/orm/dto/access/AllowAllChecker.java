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
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;

/**
 * @author pavlov
 *
 */
public class AllowAllChecker implements IDTOAccessChecker {

	@Override
	public boolean checkCreate(CommonDTO dto) {
		return true;
	}

	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {
		return true;
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {
		return true;
	}

	@Override
	public boolean checkSave(CommonDTO dto) {
		return true;
	}

}

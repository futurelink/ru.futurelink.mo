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

package ru.futurelink.mo.orm.types;

import java.util.Date;

public class DateRange {
	private Date mBeginDate;
	public void setBeginDate(Date beginDate) { mBeginDate = beginDate; }
	public Date getBeginDate() { return mBeginDate; }
	
	private Date mEndDate;
	public void setEndDate(Date endDate) { mEndDate = endDate; }
	public Date getEndDate() { return mEndDate; }
}

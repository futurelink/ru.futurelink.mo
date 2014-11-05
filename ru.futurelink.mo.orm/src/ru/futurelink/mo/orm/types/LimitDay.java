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

import java.io.Serializable;

public class LimitDay implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Тип граничного условия:
	 * 0 - до конкретного дня месяца,
	 * 1 - до Н-ного дня недели - 1-5 (первый четверг, третья пятница и т.д.),
	 * 2 - до конца месяца (с 29 по 31 в зависимости от месяца)
	 */
	private int mLimitType;
	public int getLimitType() { return mLimitType; }
	public void setLimitType(int limitType) { mLimitType = limitType; }
	
	private int mMonthDay;
	public int getMonthDay() { return mMonthDay; }
	public void setMonthDay(int monthDay) { 
		if(monthDay > 31) mMonthDay = 31; else mMonthDay = monthDay; 
	}

	private int mWeekDay;
	public int getWeekDay() { return mWeekDay; }
	public void setWeekDay(int weekDay) { 
		if (weekDay > 7) mWeekDay = 7; else mWeekDay = weekDay; 
	}
	
	private int mWeekDayNumber;
	public int getWeekDayNumber() { return mWeekDayNumber; }
	public void setWeekDayNumber(int weekDayNumber) { 
		if (weekDayNumber > 5) mWeekDayNumber = 5; else mWeekDayNumber = weekDayNumber; 
	}
	
	public void setFormat() {
		
	}
	
	@Override
	public String toString() {
		String resultStr = "";
		if (mLimitType == 0) {
			String format = "Every %d of month";
			resultStr = String.format(format, mMonthDay);
		} else {
			String format = "Every %d %d of month";
			resultStr = String.format(format, mWeekDayNumber, mWeekDay);
		}
		return resultStr;
	}

}

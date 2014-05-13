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

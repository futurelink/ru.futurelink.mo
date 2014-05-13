package ru.futurelink.mo.orm;

import java.util.Date;

/**
 * Результирующий класс модели для отображения списка истории
 * элемента данных.
 * 
 * @author pavlov
 *
 */
final public class HistoryResult extends ModelObject {

	private static final long serialVersionUID = 1L;

	public HistoryResult(String id, Date date, String operation, String objectId) {
		setId(id);
		setDate(date);
		setOperation(operation);
		setObjectId(objectId);
	}

	private Date mDate;
	public void setDate(Date date) { mDate = date; }
	public Date getDate() { return mDate; }

	private String mOperation;
	public void setOperation(String operation) { mOperation = operation; }
	public String getOperation() { return mOperation; }

	/**
	 * ID объекта
	 */
	private		String mId;	
	public 		String getId() {	return mId;	}
	public		void setId(String id) { mId = id; }
	
	private		String mObjectId;
	public		String getObjectId() { return mObjectId; }
	public		void setObjectId(String objectId) { mObjectId = objectId; }
	
	public		Boolean	getDeleteFlag() { return false; }
}

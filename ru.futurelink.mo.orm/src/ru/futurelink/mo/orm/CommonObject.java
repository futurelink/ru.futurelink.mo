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

package ru.futurelink.mo.orm;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Index;
import org.slf4j.Logger;

import ru.futurelink.mo.orm.annotations.DontCreateHistory;
import ru.futurelink.mo.orm.annotations.EnableWorkLog;
import ru.futurelink.mo.orm.exceptions.LockException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;
import ru.futurelink.mo.orm.security.UserLock;

@Entity(name = "CommonObject")
@Table(name = "OBJECT_COMMON")
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="entityClass", discriminatorType=DiscriminatorType.STRING,length=20)
public class CommonObject extends ModelObject {
	private static final long serialVersionUID = 1L;

	@Transient
	private SaveHandler 	mSaveHandler = null;

    public static final String FIELD_ID = "mId";
    public static final String FIELD_DELETEFLAG = "mDeleteFlag";

	public static final int SAVE_CREATE = 1;
	public static final int SAVE_MODIFY = 2;
	public static final int SAVE_DELETE = 3;
	
	/**
	 * Персистент менеджер нужен объекту для удобства, 
	 * объект может испоьзовать некоторые функции ОРМ для
	 * сохранения данных непосредственно из объекта. То
	 * есть объект может сохранить себя сам благодаря этому.
	 */
	@Transient
	protected IPersistentManagerSession mPersistentManagerSession;	
	final public void setPersistentManagerSession(IPersistentManagerSession pm) {
		mPersistentManagerSession = pm;
	}

	final public IPersistentManagerSession getPersistenceManagerSession() {
		return mPersistentManagerSession;
	}
	
	public static Query getSingleObjectSelectQuery(IPersistentManagerSession pms, Class<?> cls, Long id) {
		Query q = pms.getEm().createQuery("select obj from "+cls.getSimpleName()+" obj where obj.mId = :id", 
				cls);
		q.setParameter("id", id);
		return q;
	}

	/**
	 * Default constructor is protected. It's not private because
     * JPA needs to access it.
	 */
	protected CommonObject() {}
	
	/**
	 * Object constructor to use. The persistent manager session mandatory.
	 * 
	 * @param pmSession
	 */	
	public CommonObject(IPersistentManagerSession pmSession) {
		mPersistentManagerSession = pmSession;
		mDeleteFlag = false;
		if (PersistentManagerSessionUI.class.isAssignableFrom(mPersistentManagerSession.getClass())) {
			if (mCreator == null) mCreator = ((PersistentManagerSessionUI)pmSession).getUser();
			if (mAuthor == null) mAuthor = ((PersistentManagerSessionUI)pmSession).getUser();
		}
	}

	/**
	 * Object ID
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@DontCreateHistory
	@Column(name = "id", columnDefinition="VARCHAR(64)", nullable=false)
	@Accessors(getter = "getId", setter = "setId")
	private		String mId;

	@Override
	public 		String getId() {	return mId;	}
	public		void setId(String id) { mId = id; }
	
	
	/**
	 * Object deletion flag
	 */
	@Index
	@Column(name = "deleteFlag")
    @Accessors(getter = "getDeleteFlag", setter = "setDeleteFlag")
	private		Boolean mDeleteFlag;
	public		Boolean getDeleteFlag() { if (mDeleteFlag == null) return false; else return mDeleteFlag; }
	public		void	setDeleteFlag(Boolean deleteFlag) { mDeleteFlag = deleteFlag; }
	
	/**
	 * Object code
	 */
	@Index
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "code")
    @Accessors(getter = "getCode", setter = "setCode")
	private		CodeSupport	mCode;
	public 		CodeSupport	getCode() { return mCode; }

	@Transient
	private		CommonObject 	mUnmodifiedObject;
	public		CommonObject 	getUnmodifiedObject() { return mUnmodifiedObject; }
	public		void 			setUnmodifiedObject(CommonObject object) { mUnmodifiedObject = object; }

	/**
	 * Object creator user
	 */
	@Index
	@DontCreateHistory
	@JoinColumn(name = "creator", referencedColumnName = "id")
	@Accessors(getter = "getCreator", setter = "setCreator")
	private		User	mCreator;
	public 		User	getCreator() { return mCreator; }
	public		void	setCreator(User creator) { mCreator = creator; }  

	/**
	 * Object author user (who changed object last time)
	 */
	@Index
	@DontCreateHistory
	@JoinColumn(name = "author", referencedColumnName = "id")
	@Accessors(getter = "getAuthor", setter = "setAuthor")
	private		User	mAuthor;
	public 		User	getAuthor() { return mAuthor; }
	public		void	setAuthor(User author) { mAuthor = author; }  
	
	/**
	 * Object creation date and time
	 */
	@DontCreateHistory
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createDate")
	@Accessors(getter = "getCreateDate", setter = "setCreateDate")
	private		Date	mCreateDate;
	public 		void	setCreateDate(Date dt) { mCreateDate = dt; }
	public 		Date	getCreateDate() { return mCreateDate; }
	
	/**
	 * Object last modified date and time
	 */
	@DontCreateHistory
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifyDate")
	@Accessors(getter = "getModifyDate", setter = "setModifyDate")
	private		Date	mModifyDate;
	public 		void	setModifyDate(Date dt) { mModifyDate = dt; }
	public 		Date	getModifyDate() { return mModifyDate; }
	
	/**
	 * Object worklog (change log)
	 */
	@DontCreateHistory
	@JoinColumn(name = "workLog", referencedColumnName = "id")
	@Accessors(getter = "getWorkLog", setter = "setWorkLog")
	private		WorkLogSupport	mWorkLog;
	public		void			setWorkLog(WorkLogSupport w) { mWorkLog = w; }
	public		WorkLogSupport	getWorklog() { return mWorkLog; }
	
	/**
	 * Set object deletion flag
	 */
	public 		void delete() {
		mDeleteFlag = true;
	}

	/**
	 * Unset object deletion flag
	 */
	public		void recover() {
		mDeleteFlag = false;
	}
	
	/**
	 * Access persistent manager session to save object.
     *
	 * @return
	 * @throws Exception 
	 */
	public 		Object save() throws SaveException {
		mModifyDate = null;
		if (mPersistentManagerSession == null) {
            throw new SaveException("No persistent manager session on CommonObject!", null);
        }
		
		// Creation datetime in server time zone.
		if (mCreateDate == null) setCreateDate(Calendar.getInstance().getTime());
		if (PersistentManagerSessionUI.class.isAssignableFrom(mPersistentManagerSession.getClass())) {
			if (getCreator() == null) setCreator(((PersistentManagerSessionUI)mPersistentManagerSession).getAccessUser());
			if (getAuthor() == null) setAuthor(((PersistentManagerSessionUI)mPersistentManagerSession).getUser());
		}
		if (getCode() == null) { 
			mCode = new CodeSupport();
			mCode.setObject(this);
			mCode.setObjectClass(getClass().getName());
		}

        // Modification datetime in server time zone too.
		mModifyDate = Calendar.getInstance().getTime();

		Object result;
		try {
			 result = mPersistentManagerSession.save(this);
		} catch (Exception ex) {
			throw new SaveException("Ошибка при сохранении элемента в save()", ex);
		}
		
		return result;
	}

	@Override
	public void saveCommit() throws SaveException {
		if (mPersistentManagerSession.transactionIsOpened()) {
			mPersistentManagerSession.transactionCommit();
		}
	}
	
	public void refresh() {
		mPersistentManagerSession.getEm().refresh(this);
	}

	public Logger logger() {
		return mPersistentManagerSession.logger();
	}
	
	/**
	 * Флаг того, что по данному объекту создана блокировка.
	 */
	@Transient
	@DontCreateHistory
	private boolean mEditFlag;
	public boolean getEditFlag() {
		return mEditFlag;
	}

	/**
	 * Открыть объект на правку, в том числе и удаление.
	 * С блокировкой.
	 * 
	 * @return
	 * @throws LockException 
	 */
	public		Object edit() throws LockException {
		UserLock.acquireLock(mPersistentManagerSession, getClass().getSimpleName(), getId());
		mEditFlag = true;		
		return null;
	}

	/**
	 * Закрытие объекта с разблокировкой.
     *
	 * @throws LockException 
	 */
	public		void close() throws LockException {		
		UserLock.releaseLock(mPersistentManagerSession, getClass().getSimpleName(), getId());
		mUnmodifiedObject = null;
		mEditFlag = false;
	}

	/**
	 * Принудительное обновление поля (использовать с осторожностью!)
	 * 
	 * Иногда бывает необходимо обойти встроенные механизмы проверки изменения 
	 * объектов в JPA. 
	 * 
	 * Например, в случае связи исторических элментов с неисторическими.
	 * Если элемент должен быть, например, пересвязан, а JPA не гарантирует, что поле будет
	 * перезаписано (например оно считает, что связанный объект не изменился), в случае
	 * если изменилась только ID связанного элемента, а сами данные - нет; в этом случае
	 * нужно пересвязать элемент принудительно, чтобы гарантировать то он не отвяжется
	 * в какой-нибудь неудобный момент.
	 * 
	 * @param field
	 * @param dataItem
	 */
	public void forceUpdateField(String field, CommonObject dataItem) {
		Query q = mPersistentManagerSession.getEm().createQuery(
			"update "+getClass().getSimpleName()+" set "+field+" = :param where mId = :id"
		);
		q.setParameter("param", dataItem);
		q.setParameter("id", getId());	
		q.executeUpdate();
	}
	
	/**
	 * Установить обработчик записи элемента данных.
     *
	 * @param handler
	 */
	final public 	void setSaveHandler(SaveHandler handler) {
		mSaveHandler = handler;
	}
	
	/**
	 * Обработка перед сохранением объекта
     *
	 * @param saveFlag
	 * @return
	 */	
	protected 	Object onBeforeSave(int saveFlag) throws SaveException {
		if (mSaveHandler != null) {
			return mSaveHandler.onBeforeSave();
		}
		return null;
	}

	/**
	 * Обработка после сохранения объекта
     *
	 * @param saveFlag
	 * @return
	 */
	protected 	Object onAfterSave(int saveFlag) throws SaveException {
		if (mSaveHandler != null) {
			return mSaveHandler.onAfterSave();
		}
		mUnmodifiedObject = null;		
		return null;
	}

	/**
	 * При уничтожении объекта удаляем блокировку.
	 */
	@Override
	protected void finalize() {
		if (mEditFlag) {
			try {			
				close();
			} catch (LockException e) {
			}
		}
	}
	
	/**
	 * Получить данные о том, используется ли лог действий в данном элементе.
	 * Опция определяется аннотацией EnableWorkLog.
     *
	 * @return
	 */
	protected boolean getWorkLogSupport() {
		EnableWorkLog ewl = getClass().getAnnotation(EnableWorkLog.class);
		if (ewl != null) {
			return true;
		}		
		return false;
	}

}

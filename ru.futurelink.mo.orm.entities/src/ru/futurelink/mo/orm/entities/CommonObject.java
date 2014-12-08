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

package ru.futurelink.mo.orm.entities;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Index;
import org.slf4j.Logger;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.SaveHandler;
import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.annotations.DontCreateHistory;
import ru.futurelink.mo.orm.annotations.EnableWorkLog;
import ru.futurelink.mo.orm.exceptions.LockException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;
import ru.futurelink.mo.orm.pm.PersistentManagerSessionUI;
import ru.futurelink.mo.orm.entities.security.User;
import ru.futurelink.mo.orm.entities.security.UserLock;
import ru.futurelink.mo.orm.iface.ICodeSupport;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IUser;
import ru.futurelink.mo.orm.iface.IWorkLog;

@Entity(name = "CommonObject")
@Table(name = "OBJECT_COMMON")
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="entityClass", discriminatorType=DiscriminatorType.STRING,length=20)
public class CommonObject 
	extends ModelObject 
	implements ICommonObject {
	private static final long serialVersionUID = 1L;

	@Transient
	private SaveHandler 	mSaveHandler = null;

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
		Query q = pms.getEm().createQuery("select obj from "+cls.getSimpleName()+" obj where obj."+FIELD_ID+" = :id", 
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
		deleteFlag = false;
		if (PersistentManagerSessionUI.class.isAssignableFrom(mPersistentManagerSession.getClass())) {
			if (mCreator == null) mCreator = (User) ((PersistentManagerSessionUI)pmSession).getUser();
			if (mAuthor == null) mAuthor = (User) ((PersistentManagerSessionUI)pmSession).getUser();
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
	private		String id;

	@Override
	public 		String getId() {	return id;	}
	public		void setId(String id) { this.id = id; }
	
	
	/**
	 * Object deletion flag
	 */
	@Index
	@Column(name = "deleteFlag")
    @Accessors(getter = "getDeleteFlag", setter = "setDeleteFlag")
	private		Boolean deleteFlag;
	public		Boolean getDeleteFlag() { if (this.deleteFlag == null) return false; else return deleteFlag; }
	public		void	setDeleteFlag(Boolean deleteFlag) { this.deleteFlag = deleteFlag; }
	
	/**
	 * Object code
	 */
	@Index
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "code")
    @Accessors(getter = "getCode", setter = "setCode")
	private		CodeSupport	mCode;
	public 		ICodeSupport	getCode() { return mCode; }

	@Transient
	private		CommonObject 	mUnmodifiedObject;
	public		ICommonObject 	getUnmodifiedObject() { return mUnmodifiedObject; }
	public		void 			setUnmodifiedObject(ICommonObject object) { mUnmodifiedObject = (CommonObject) object; }

	/**
	 * Object creator user
	 */
	@Index
	@ManyToOne
	@DontCreateHistory
	@JoinColumn(name = "creator", referencedColumnName = "id")
	@Accessors(getter = "getCreator", setter = "setCreator")
	private		User	mCreator;
	public 		IUser	getCreator() { return mCreator; }
	public		void	setCreator(IUser creator) { mCreator = (User) creator; }  

	/**
	 * Object author user (who changed object last time)
	 */
	@Index
	@ManyToOne
	@DontCreateHistory
	@JoinColumn(name = "author", referencedColumnName = "id")
	@Accessors(getter = "getAuthor", setter = "setAuthor")
	private		User	mAuthor;
	public 		IUser	getAuthor() { return mAuthor; }
	public		void	setAuthor(IUser author) { mAuthor = (User) author; }  
	
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
	@ManyToOne
	@JoinColumn(name = "workLog", referencedColumnName = "id")
	@Accessors(getter = "getWorkLog", setter = "setWorkLog")
	private		WorkLogSupport	mWorkLog;
	public		void		setWorkLog(IWorkLog w) { mWorkLog = (WorkLogSupport) w; }
	public		IWorkLog	getWorklog() { return mWorkLog; }
	
	/**
	 * Set object deletion flag
	 */
	public 		void delete() {
		deleteFlag = true;
	}

	/**
	 * Unset object deletion flag
	 */
	public		void recover() {
		deleteFlag = false;
	}
	
	/**
	 * Access persistent manager session to save object.
     *
	 * @return
	 * @throws Exception 
	 */
	@Override
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
			//mCode.setObject(this);
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
	@Override
	public void forceUpdateField(String field, ICommonObject dataItem) {
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
	@Override
	public 	Object onBeforeSave(int saveFlag) throws SaveException {
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
	@Override
	public 	Object onAfterSave(int saveFlag) throws SaveException {
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
	@Override
	public boolean getWorkLogEnabled() {
		EnableWorkLog ewl = getClass().getAnnotation(EnableWorkLog.class);
		if (ewl != null) {
			return true;
		}		
		return false;
	}

	@Override
	public IWorkLog createWorkLog() {
		return new WorkLogSupport(getPersistenceManagerSession());
	}
	
}

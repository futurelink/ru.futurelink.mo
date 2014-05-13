package ru.futurelink.mo.orm;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Transient
	private SaveHandler 	mSaveHandler = null;
	
	// Флаг типа сохранения
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
	protected PersistentManager mPersistentManager;	
	final public void setPersistentManager(PersistentManager pm) {
		mPersistentManager = pm;
	}

	final public PersistentManager getPersistenceManager() {
		return mPersistentManager;
	}
	
	public static Query getSingleObjectSelectQuery(PersistentManager pm, Class<?> cls, Long id) {
		Query q = pm.getEm().createQuery("select obj from "+cls.getSimpleName()+" obj where obj.mId = :id", 
				cls);
		q.setParameter("id", id);
		return q;
	}

	/**
	 * Конструктор сделан приватным, 
	 * чтобы его нельзя было использовать.
	 */
	protected CommonObject() {}
	
	/**
	 * Правильный конструктор, который позволяет создать объект сразу
	 * с менеджером хранения.
	 * 
	 * @param manager
	 */	
	public CommonObject(PersistentManager manager) {
		mPersistentManager = manager;
		mDeleteFlag = false;
		if (mCreator == null) mCreator = manager.getUser();
		if (mAuthor == null) mAuthor = manager.getUser();
	}

	/**
	 * ID объекта
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
	 * Флаг удаления или устаревания элемента данных.
	 */
	@Index
	@Column(name = "deleteFlag")
	private		Boolean mDeleteFlag;
	public		Boolean getDeleteFlag() { if (mDeleteFlag == null) return false; else return mDeleteFlag; }
	public		void	setDeleteFlag(Boolean deleteFlag) { mDeleteFlag = deleteFlag; }
	
	/**
	 * Код элемента данных, идентификатор не зависящий от ID.
	 */
	@Index
	@ManyToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "code")
	private		CodeSupport	mCode;
	public 		CodeSupport	getCode() { return mCode; }

	@Transient
	private		CommonObject 	mUnmodifiedObject;
	public		CommonObject 	getUnmodifiedObject() { return mUnmodifiedObject; }
	public		void 			setUnmodifiedObject(CommonObject object) { mUnmodifiedObject = object; }

	/**
	 * Создатель элемента
	 */
	@Index
	@DontCreateHistory
	@JoinColumn(name = "creator", referencedColumnName = "id")
	@Accessors(getter = "getCreator", setter = "setCreator")
	private		User	mCreator;
	public 		User	getCreator() { return mCreator; }
	public		void	setCreator(User creator) { mCreator = creator; }  

	/**
	 * Автор элемента
	 */
	@Index
	@DontCreateHistory
	@JoinColumn(name = "author", referencedColumnName = "id")
	@Accessors(getter = "getAuthor", setter = "setAuthor")
	private		User	mAuthor;
	public 		User	getAuthor() { return mAuthor; }
	public		void	setAuthor(User author) { mAuthor = author; }  
	
	/**
	 * Дата создания элемента.
	 */
	@DontCreateHistory
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createDate")
	@Accessors(getter = "getCreateDate", setter = "setCreateDate")
	private		Date	mCreateDate;
	public 		void	setCreateDate(Date dt) { mCreateDate = dt; }
	public 		Date	getCreateDate() { return mCreateDate; }
	
	/**
	 * Дата последнего изменения элемента.
	 */
	@DontCreateHistory
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifyDate")
	@Accessors(getter = "getModifyDate", setter = "setModifyDate")
	private		Date	mModifyDate;
	public 		void	setModifyDate(Date dt) { mModifyDate = dt; }
	public 		Date	getModifyDate() { return mModifyDate; }
	
	/**
	 * Элемент ворклога
	 */
	@DontCreateHistory
	@JoinColumn(name = "workLog", referencedColumnName = "id")
	@Accessors(getter = "getWorkLog", setter = "setWorkLog")
	private		WorkLogSupport	mWorkLog;
	public		void			setWorkLog(WorkLogSupport w) { mWorkLog = w; }
	public		WorkLogSupport	getWorklog() { return mWorkLog; }
	
	/**
	 * Удалить текущий объект, проставить ему флаг удаления
	 */
	public 		void delete() {
		mDeleteFlag = true;
	}

	/**
	 * Восстановить текущий объект, снять ему флаг удаления
	 */
	public		void recover() {
		mDeleteFlag = false;
	}
	
	/**
	 * Выполнить персист объекта и обработки сохранения.
	 * @return
	 * @throws Exception 
	 */
	public 		Object save() throws SaveException {
		mModifyDate = null;
		if (mPersistentManager == null) { throw new SaveException("No persistent manager on CommonObject!", null); }
		
		// Дата создания и модификации - во временной зоне GMT.
		if (mCreateDate == null) setCreateDate(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
		if (getCreator() == null) setCreator(mPersistentManager.getAccessUser());
		if (getAuthor() == null) setAuthor(mPersistentManager.getUser());
		if (getCode() == null) { 
			mCode = new CodeSupport();
			mCode.setObject(this);
			mCode.setObjectClass(getClass().getName());
		}
		
		// Дата изменения тоже во временной зоне GMT.
		mModifyDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		
		Object result;
		try {
			 result = mPersistentManager.save(this);
		} catch (Exception ex) {
			throw new SaveException("Ошибка при сохранении элемента в save()", ex);
		}
		
		return result;
	}
	
	public void refresh() {
		mPersistentManager.getEm().refresh(this);
	}
	
	public Logger logger() {
		return mPersistentManager.logger();
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
		UserLock.acquireLock(mPersistentManager, getClass().getSimpleName(), getId());
		mEditFlag = true;		
		return null;
	}

	/**
	 * Закрытие объекта с разблокировкой.
	 * @throws LockException 
	 */
	public		void close() throws LockException {		
		UserLock.releaseLock(mPersistentManager, getClass().getSimpleName(), getId());
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
		Query q = mPersistentManager.getEm().createQuery("update "+getClass().getSimpleName()+" set "+field+" = :param where mId = :id");
		q.setParameter("param", dataItem);
		q.setParameter("id", getId());
		
		mPersistentManager.getEm().getTransaction().begin();
		q.executeUpdate();
		mPersistentManager.getEm().getTransaction().commit();
	}
	
	/**
	 * Установить обработчик записи элемента данных.
	 * @param handler
	 */
	final public 	void setSaveHandler(SaveHandler handler) {
		mSaveHandler = handler;
	}
	
	/**
	 * Обработка перед сохранением объекта
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

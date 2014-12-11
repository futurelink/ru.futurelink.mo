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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.annotations.DontCreateHistory;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.IHistoryObject;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

/**
 * Исторический объект.
 * 
 * @author Futurelink
 * @since 0.1
 *
 */
@Entity(name = "HistoryObject")
@MappedSuperclass
@Table(name = "OBJECT_HISTORY")
public class HistoryObject 
	extends CommonObject
	implements IHistoryObject {

	private static final long serialVersionUID = 1L;
	
	protected HistoryObject() {}
	
	public HistoryObject(IPersistentManagerSession session) {
		super(session);
	}
	
	/**
	 * Тестовое поле, не стоит его использовать в программах,
	 * оно используется только в автоматических юнит-тестах.
	 */
	public String mTest;
	
	/**
	 * Поле содержащее ID следующего элемента.
	 */
	@Transient
	@DontCreateHistory	
	protected String mNextId = null;
	public String getNextId() { return mNextId; }
	public void setNextId(String id) { mNextId = id; }	
	
	/**
	 *  Предыдущий элемент данных, хранимое поле.
	 */
	@DontCreateHistory
	@Column(name = "prevId")
	protected String mPrevId;
	public void setPrevId(String id) { mPrevId = id; }
	public String getPrevId() { return mPrevId; }

	@Index
	@DontCreateHistory
	@Column(name = "outdated")
	@Accessors(getter = "getOutdated", setter = "setOutdated")
	protected boolean mOutdated;
	public void setOutdated(boolean outdated) { mOutdated = outdated; }
	public boolean getOutdated() { return mOutdated; }
	
	/**
	 * Получить объект предыдущего элемента, ищет объект по
	 * методом getPrevObjectId(), поднимает объект и возвращает его.
	 * @return
	 */
	@Transient
	public HistoryObject getPrevObject() {
		HistoryObject obj = mPersistentManagerSession.getEm().find(HistoryObject.class, mPrevId);
		return obj; 
	}

	/**
	 * Получить следующий объект, ищет объект у которого предыдущий
	 * выставлен текущим ID и возвращает объект.
	 * @return
	 */
	@Transient
	public HistoryObject getNextObject() {
		Query q = mPersistentManagerSession.getEm().createQuery(
			"select obj from "+this.getClass().getSimpleName()+" obj where obj.prevId = :id"
		);
		q.setParameter("id", getId());
		HistoryObject obj = (HistoryObject) q.getSingleResult();
		return obj; 
	}
	
	/**
	 * Метод проверяет надо ли создавать историю элемента при данных
	 * изменениях в объекте. Некоторым полям в классе Entity можно выставить
	 * аннотацию DontCreateHistory - она будет говорить о том, что если данное поле
	 * изменяется история не будет создаваться. При этом, если изменится какое-то поле,
	 * которое не имеет такой аннотации история будет создана как обычно.
	 * @return
	 * @throws SaveException 
	 */
	private boolean checkDontCreateHistory() throws SaveException {
		if (getUnmodifiedObject() != null) {
			// Собираем информацию о полях класса и его предков
			Class<?> cls = getClass();
			ArrayList<Field> fields = new ArrayList<Field>();
			while (cls != Object.class) {
				Field fieldList[] = cls.getDeclaredFields();
				fields.addAll(Arrays.asList(fieldList));
				cls = cls.getSuperclass();
			}
			for(int i = 0; i < fields.size(); i++) {			
				// Проверяем что значение поля изменилось, а аннотации чтобы не создавать
				// исторический элемент для него не указано.
				if ((fields.get(i).getAnnotation(Transient.class) == null) &&
					(fields.get(i).getAnnotation(Id.class) == null)) {
					DontCreateHistory dchFlag = fields.get(i).getAnnotation(DontCreateHistory.class);
					Transient transFlag = fields.get(i).getAnnotation(Transient.class);
					if ((dchFlag == null) && (transFlag != null)) {
						fields.get(i).setAccessible(true);
						try {
							if (fields.get(i).get(getUnmodifiedObject()) != null) {
								if (!fields.get(i).get(getUnmodifiedObject()).equals(fields.get(i).get(this))) {
									return false;					
								}
							} else {
								if (fields.get(i).get(this) != null) {
									return false;
								}
							}
						} catch (IllegalAccessException | IllegalArgumentException ex) {
							throw new SaveException("Error in checkDontCreateHistory()", ex);
						}
					}		
				}
			}
		}
		return true;
	}
	
	/**
	 * Сохранение элмента с записью истории.
	 * При этом ID элемента увеличивается, создается новый элемент и
	 * проставляются ссылки на историю элемента. 
	 */
	@Override
	public Object save() throws SaveException {
		Object retVal = null;
		if (mPersistentManagerSession == null) { 
			throw new SaveException("No persistent manager on HistoryObject!", null); 
		}

		try {
			String oldId = getId();						// Сохраняем старую ID
			if (oldId != null) {
				// Перед сохранением сохраняем неизмененную копию объекта
				// это актуально если объект сохраняется, а потом изменяется
				// тот же экземпляр и снова сохраняется.
				setUnmodifiedObject(mPersistentManagerSession.getOldEm().find(getClass(), oldId));

				if (checkDontCreateHistory()) {
					// Сохраняем элемент без создания исторического элемента,
					// если по итогу вычислений не надо создавать исторических
					// элментов.
					retVal = super.save();
				} else {										
					retVal = mPersistentManagerSession.saveWithHistory(this, oldId);
				}
			} else {
				// Если объект создается в первый раз, то
				// мы используем простое сохранение без всяких изысков.
				retVal = super.save();				
			}
		} catch (OpenException ex) {
			throw new SaveException("Error reopening HistoryObject after saving", ex);
		}
		return retVal;
	}
}

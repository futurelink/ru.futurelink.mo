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

package ru.futurelink.mo.orm.dto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IModelObject;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;
import ru.futurelink.mo.orm.pm.PersistentObjectFactory;

/**
 * Объект переноса данных.
 * 
 * Созданный объект переноса данных служит для передачи
 * данных сформированных из ОРМ модели, и одновременного ограничения
 * доступа к самой модели. 
 * 
 * Объект DTO должен быть создан только для передачи данных и потом удален,
 * так как по сути дублирует данные, которые содержатся в ОРМ модели. 
 * 
 * Изоляция модели данных реализована в виде буфера изменений,
 * публичный доступ к которому отсутствует. При том, любой объект,
 * который лично знает про модель данных может при помощи рефлексии
 * записать данные в модель.
 *  
 * @author Futurelink
 * @since 0.1
 *
 */
public class EditorDTO extends CommonDTO
	implements IEditorDTO {
	private static final long serialVersionUID = 1L;

	protected HashMap<String, Object[]> 	mChangesBuffer;

	public EditorDTO(IModelObject dataItem) {
		super(dataItem);

		// Структура буфера изменений: "название поля": [ геттер, сеттер, значение ]
		mChangesBuffer = new HashMap<String, Object[]>();
	}

	protected IPersistentManagerSession getPersistenceManagerSession() {
		return ((ICommonObject)mData).getPersistenceManagerSession();
	}
	
	public static IEditorDTO create(
			Class <? extends ICommonObject> dataClass, 
			IPersistentManagerSession session, 
			IDTOAccessChecker accessChecker) throws DTOException {		
		IEditorDTO dto = PersistentObjectFactory.getInstance().
				createEditorDTO(dataClass, EditorDTO.class, session, accessChecker);	

		return dto;		
	}

	@Override
	public void save() throws DTOException, SaveException {
		if (getAccessChecker() == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		EditorDTO.applyChanges(mData, this);

		// Проверка права на сохранение объекта
		if (!getAccessChecker().checkSave(this)) {
			throw new DTOAccessException("У вас нет права на сохранение этого элемента.", null);
		}
		((ModelObject)mData).save();
	}

	@Override
	public void saveCommit() throws SaveException {
		((ModelObject)mData).saveCommit();
	}
	
	/**
	 * Получить буфер изменений по объекту DTO.
	 * 
	 * @return
	 */
	@Override
	public HashMap<String, Object[]> getChangesBuffer() {
		return mChangesBuffer;
	}

	/**
	 * Очистить буфер изменений. 
	 */
	@Override
	public void clearChangesBuffer() {
		mChangesBuffer.clear();
	}

	@Override
	public void delete(boolean hardDelete) throws DTOException {
		if (getAccessChecker() == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!hardDelete) {
			setDataField(ModelObject.FIELD_DELETEFLAG, "getDeleteFlag", "setDeleteFlag", true, true);
		} else {
			// TODO Жесткое удаление из базы навсегда!			
		}
	}
	
	@Override
	public void recover() throws DTOException {
		setDataField(ModelObject.FIELD_DELETEFLAG, "getDeleteFlag", "setDeleteFlag", false, true);
	}
	
	/**
	 * Получить данные о том, есть ли изменения в буфере и отличаются ли
	 * они от оригинала. А также, какие именно поля изменены.
	 * 
	 * @return
	 * @throws DTOException 
	 */
	@Override
	public ArrayList<String> getChangedData() throws DTOException {
		ArrayList<String> changedList = new ArrayList<String>();
		
		int changesCount = getChangesBuffer().keySet().size();
		for (int i = 0; i < changesCount; i++) {
			String key = (String) getChangesBuffer().keySet().toArray()[i];
			Object[] values = getChangesBuffer().get(key);
			try {
		        Method getValueMethod = mData.getClass().getMethod(values[0].toString());
		        Object a = getValueMethod.invoke(mData);
		        // Если у нас в values[2] находится ссылка на DTO, надо
		        // сравнивать объект, который в этом DTO используется, а не сам DTO.
		        if ((values[2] != null) && CommonDTO.class.isAssignableFrom(values[2].getClass())) {
		        	if (((a == null) && (values[2] != null)) ||
		        		((a != null) && (!a.equals(((CommonDTO)values[2]).mData))))
		        		changedList.add(key);
		        } else {
		        	if (((a == null) && (values[2] != null)) ||
		        		((a != null) && !a.equals(values[2]))) {
		        		changedList.add(key);
		        	}
		        }
			} catch (NoSuchMethodException e) {
				throw new DTOException(e.toString(), e);
			} catch (IllegalAccessException e) {
				throw new DTOException(e.toString(), e);
			} catch (InvocationTargetException e) {
				throw new DTOException(e.toString(), e);
			}
		}		

		return changedList;
	}

	/**
	 * Статический метод сохранения данные в модели из
	 * буфера изменений. Требуется, чтобы вызывающий объект знал
	 * про модель и DTO. Метод реализует изоляцию изменений от
	 * просмотрщика, который знает только о DTO.
	 * 
	 * @param data
	 * @param dto
	 * @throws DTOException
	 */
	
	public static void applyChanges(IModelObject data, CommonDTO dto) throws DTOException {
		if (dto.getAccessChecker() == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!dto.getAccessChecker().checkSave(dto)) {
			throw new DTOAccessException("У вас нет права на сохранение этого элемента.", null);
		}

		int changesCount = dto.getChangesBuffer().keySet().size();
		for (int i = 0; i < changesCount; i++) {
			String key = (String) dto.getChangesBuffer().keySet().toArray()[i];
			Object[] values = dto.getChangesBuffer().get(key);
			if (values[1] == null) {
				dto.logger().debug("Поле сконфигурировано только для чтения т.к. сеттер не задан, пропускаем.");
			} else {
				try {
					Method setValueMethod;
					if (values[2] == null) {
						dto.logger().debug("Вызываем {} для сохранения пустого значения.", values[1]);
					} else {
						dto.logger().debug("Вызываем {} для сохранения {}",values[1], values[2]);					
					}
					Class<?> fieldClass = data.getClass().getMethod(values[0].toString()).getReturnType();
					if ((values[2] != null) && (values[2].getClass().equals(EditorDTO.class))) {
						// Если в значение нам передано DTO, то надо из него получить данные, и объект
						// посетить как свойство.
						for (Method method : data.getClass().getMethods()) {
							if (values[1].toString().equals(method.getName())) {
								if (method.getParameterTypes()[0].isAssignableFrom(
									((EditorDTO)values[2]).mData.getClass())
								) {
									setValueMethod = method;
									setValueMethod.invoke(data, 
										values[2] != null ? (((EditorDTO)values[2]).mData) : fieldClass
									);
									break;
								}
							}
						}					
					} else {
						// Если передано обычное значение, то просечиваем его как 
						// значение определенного типа
						boolean methodInvoked = false;
						for (Method method : data.getClass().getMethods()) {
							if (values[1].toString().equals(method.getName())) {
								if (method.getParameterTypes()[0].isAssignableFrom(
									values[2] != null ? values[2].getClass() : fieldClass)
								) {
									method.invoke(data, values[2]);
									methodInvoked = true;
									break;
								}
							}
						}
						if (!methodInvoked) throw new NoSuchMethodException(
								"Нет подходящего метода "+values[1].toString()+" для сохранения данных типа "+
								values[2].getClass().getSimpleName()
							);
					}
				} catch (NoSuchMethodException e) {
					throw new DTOException(e.toString(), e);
				} catch (IllegalAccessException e) {
					throw new DTOException(e.toString(), e);
				} catch (InvocationTargetException e) {
					throw new DTOException(e.toString(), e);
				} catch (SecurityException e) {
					throw new DTOException(e.toString(), e);
				}
			}
		}		
		dto.getChangesBuffer().clear();
	}

	/**
	 * Сохраняет данные в буфер изменений. Значение проверяется на изменение, если значение не изменялось
	 * в буфере изменений ничего не будет сохранено.
	 * 
	 * @param fieldSetterName
	 * @param value
	 * @throws DTOException
	 */	
	@Override
	public void setDataField(String fieldName, String fieldGetterName, String fieldSetterName, Object value) throws DTOException {
		setDataField(fieldName, fieldGetterName, fieldSetterName, value, false);
	}
	
	/**
	 * Сохраняет данные в буфер изменений.
	 * 
	 * @param fieldSetterName
	 * @param value
	 * @param force насильно сохранить значение, не проверять на то, реально ли оно изменено
	 * @throws DTOException
	 */
	@Override
	public void setDataField(String fieldName, String fieldGetterName, String fieldSetterName, Object value, boolean force) throws DTOException {
		if (getAccessChecker() == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!getAccessChecker().checkWrite(this, fieldName)) {
			throw new DTOAccessException("У вас нет права на изменение этого элемента.", null);
		}
		
		// Если пытаются получить данные, а объекта
		// ОРМ просто нет, то выкидываем специфический эксепшн.
		if (mData == null)
			throw new DTOException("Элемент данных = null", null);

		// Если сеттер не предусмотрен, то ничего не делаем тут.
		if (fieldSetterName == null) return;
		
		try {
			// Проверяем на доступность метод, по рефлексии
			Object oldValue = null;
			Class<?> fieldClass = mData.getClass().getMethod(fieldGetterName).getReturnType();
			Method getValueMethod;
				
			// Если поле, в которое надо записать объект - унаследовано от CommonObject, то
			getValueMethod = mData.getClass().getMethod(fieldGetterName);
			oldValue = getValueMethod.invoke(mData);
			boolean propModified = false;
			if (!force) {
				if (ICommonObject.class.isAssignableFrom(fieldClass)) {
					//mData.getClass().getMethod(fieldSetterName, fieldClass);
					if ((oldValue == null && value != null) ||
						(value == null && oldValue != null) ||
						(oldValue != null && value != null && 
						!oldValue.equals(((EditorDTO)value).mData)))
						propModified = true;
				} else {							
					//mData.getClass().getMethod(fieldSetterName, value.getClass());			
					if ((oldValue == null && value != null) ||
						(oldValue != null && !oldValue.equals(value)))
					propModified = true;
				}
			}
			
			// Если свойство изменилось по сравнению с данными в базе,
			// если мы принудитально засовываем его в буфер изменени по force,
			// или если свойство уже в буфере изменений есть - перезапишем его туда!
			if (propModified || force || mChangesBuffer.containsKey(fieldName)) {
				// Сохраняем изменения в буфере изменений
				Object[] data = new Object[3];
				data[0] = fieldGetterName;
				data[1] = fieldSetterName;
				data[2] = value;			
				mChangesBuffer.put(fieldName, data);
			}
		} catch (SecurityException e) {
			throw new DTOException(e.toString(), e);
		} catch (NoSuchMethodException e) {
			throw new DTOException(e.toString(), e);
		} catch (IllegalAccessException e) {
			throw new DTOException(e.toString(), e);
		} catch (IllegalArgumentException e) {
			throw new DTOException(e.toString(), e);
		} catch (InvocationTargetException e) {
			throw new DTOException(e.toString(), e);
		}
	}
	
	/**
	 * Получает данные из модели ОРМ, путем вызова геттера, имя которого
	 * передается в метод. Если поле унаследовано от CommonObject, то для него
	 * создаяется объект EditorDTO, и возвращается в качестве значения этого поля. 
	 * 
	 * @param fieldGetterName имя геттера в модели данных.
	 * @return
	 * @throws DTOException
	 */
	@Override
	public Object getDataField(String fieldName, String fieldGetterName, String fieldSetterName, boolean checkAccess) throws DTOException {
		if (getAccessChecker() == null && checkAccess) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (checkAccess && !getAccessChecker().checkRead(this, fieldName)) {
			DTOAccessException ex = new DTOAccessException("У вас нет права на получение данных из этого элемента.", null);
			ex.setAccessData("EditorDTO get data from field: name is "+fieldName+" getter is "+fieldGetterName);
			throw ex;
		}

		Object a = null;

		// Если пытаются получить данные, а объекта
		// ОРМ просто нет, то выкидываем специфический эксепшн.
		if (mData == null)
			throw new DTOException("Элемент данных = null", null);
		
		// Если нет геттера или имени поля - это не ошибка,
		// но мы вернем null, чтобы не повадно было! 
		if ((fieldGetterName == null) || (fieldName == null))
			return null;

		try {
			Class<?> fieldClass = mData.getClass().getMethod(fieldGetterName).getReturnType();
	        Method getValueMethod = mData.getClass().getMethod(fieldGetterName);
	        if (ICommonObject.class.isAssignableFrom(fieldClass)) {
	        	// Нужно генерить DTO на основании объекта только тогда,
	        	// когда этот объект, то есть значение поля в ORM не null.
	        	ICommonObject obj = (ICommonObject) getValueMethod.invoke(mData);
	        	if (obj != null) {
	        		obj.setPersistentManagerSession(getPersistenceManagerSession());
		        	a = new EditorDTO((ModelObject)obj);
		        	((CommonDTO)a).addAccessChecker(getAccessChecker());
	        	}
	        } else {
	        	a = getValueMethod.invoke(mData);
	        }
	        
	        // Приоритетными при этом вызове являются данные из буфера изменений,
	        // если данные в буфере изменений отличаются от данных в элементе данных,
	        // то берем в возвращаем их. Разумеется, если в буфере вообще есть данные
	        // по этому ключу, то есть данные вообще когда либо изменялись.
        	if (mChangesBuffer.containsKey(fieldName)) {
        		if ((mChangesBuffer.get(fieldName)[2] != null) &&
                	!mChangesBuffer.get(fieldName)[2].equals(a)) {
            		a = mChangesBuffer.get(fieldName)[2];
        		} else if (mChangesBuffer.get(fieldName)[2] == null) {
        			a = null;
        		}
	        }
		} catch (NoSuchMethodException e) {
			throw new DTOException(e.toString(), e);
		} catch (IllegalAccessException e) {
			throw new DTOException(e.toString(), e);
		} catch (InvocationTargetException e) {
			throw new DTOException(e.toString(), e);
		} catch (SecurityException e) {
			throw new DTOException(e.toString(), e);
		}
		
		return a;
	}
	
	@Override
	public Long getCode() {
		if (mData != null)
			return ((ICommonObject)mData).getCode().getId();
		else
			return (long)0;	
	}

	@Override
	public void clear() {
		clearChangesBuffer();
		mData = null;
	}

	@Override
	public void forceUpdateField(String field, CommonDTO dto) {
		((ICommonObject)mData).forceUpdateField(field, (ICommonObject)dto.mData);
	}

	@Override
	public void refresh() {
		getPersistenceManagerSession().getEm().refresh(mData);
	}
}

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.IPersistentManagerSession;
import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.PersistentManagerSession;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;

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
public class EditorDTO extends CommonDTO {
	private static final long serialVersionUID = 1L;

	protected HashMap<String, Object[]> 	mChangesBuffer;

	public EditorDTO(ModelObject dataItem) {
		super(dataItem);

		// Структура буфера изменений: "название поля": [ геттер, сеттер, значение ]
		mChangesBuffer = new HashMap<String, Object[]>();
	}

	protected IPersistentManagerSession getPersistenceManagerSession() {
		return ((CommonObject)mData).getPersistenceManagerSession();
	}
	
	public static EditorDTO create(Class <? extends CommonObject> dataClass, PersistentManagerSession session, IDTOAccessChecker accessChecker) throws DTOException {
		Constructor<? extends CommonObject> cons = null;
		CommonObject						data = null;
		try {
			cons = dataClass.getConstructor(PersistentManagerSession.class);
			data = cons.newInstance(session);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new DTOException("Ошибка вызова create для EditorDTO.", e);
		}
		EditorDTO dto = new EditorDTO(data);
		dto.addAccessChecker(accessChecker);
		return dto;
		
	}

	@Override
	public void save() throws DTOException, SaveException {
		if (mAccessChecker == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		EditorDTO.applyChanges((CommonObject) mData, this);

		// Проверка права на сохранение объекта
		if (!mAccessChecker.checkSave(this)) {
			throw new DTOAccessException("У вас нет права на сохранение этого элемента.", null);
		}
		((ModelObject)mData).save();
	}

	@Override
	public void saveCommit() throws SaveException {
		((ModelObject)mData).saveCommit();
	}
	
	/**
	 * Метод создает список DTO из списка элементов типа CommonObject. Если список
	 * результата запроса пустой, то возвращается пустой список DTO.
	 * 
	 * @param resultList
	 * @return
	 */
	public static Map<String, EditorDTO> fromResultList(
			List<? extends CommonObject> resultList, 
			PersistentManagerSession pm, 
			Class<? extends CommonDTO> itemDTOClass,
			IDTOAccessChecker accessChecker) throws DTOException {
		Map<String, EditorDTO> list = new HashMap<String, EditorDTO>();
		if ((resultList != null) && (resultList.size() > 0)) {
			for (Object c : resultList) {
				((CommonObject)c).setPersistentManagerSession(pm);

				// Создаем DTO рефлексией
				try {
					Constructor<? extends CommonDTO> ctr = itemDTOClass.getConstructor(CommonObject.class);
					EditorDTO dto = (EditorDTO)ctr.newInstance((CommonObject)c);
					if (dto != null) {
						dto.addAccessChecker(accessChecker);
						list.put(((CommonObject)c).getId(), dto);
					} else {
						throw new DTOException("Ошибка, созданный DTO = null", null);
					}
				} catch (NoSuchMethodException | 
						SecurityException | 
						DTOException | 
						InstantiationException | 
						IllegalAccessException | 
						IllegalArgumentException | 
						InvocationTargetException e) {
					throw new DTOException("Невозможно создать объект DTO в fromResultList.", e);
				} 
			}

		}
		return list;
	}

	/**
	 * Простой метод заполнения списка данных, привязанного к
	 * отображению, перед переходом на асинхронные отображения.
	 * 
	 * @param result
	 * @param sourceList
	 * @param pm
	 * @param itemDTOClass
	 * @param accessChecker
	 */
	public static void fillResultList(
			Map<String, ? extends CommonDTO> resultList,
			List<?> sourceList, 
			PersistentManagerSession pm, 
			Class<? extends CommonDTO> itemDTOClass,
			IDTOAccessChecker accessChecker			
		) throws DTOException  {
		
		@SuppressWarnings("unchecked")
		Map<String, EditorDTO>localDTO = (Map<String, EditorDTO>) resultList;
		
		if ((sourceList == null) || (sourceList.size() == 0)) {
			resultList.clear();
			return;
		} else {
			for (Object c : sourceList) {
				((CommonObject)c).setPersistentManagerSession(pm);
				
				// Создаем DTO рефлексией
				try {
					Constructor<? extends CommonDTO> ctr = itemDTOClass.getConstructor(CommonObject.class);
					EditorDTO dto = (EditorDTO)ctr.newInstance((CommonObject)c);
					if (dto != null) {
						dto.addAccessChecker(accessChecker);
						localDTO.put(
							((CommonObject)c).getId(),
							dto
						);
					} else {
						throw new DTOException("Ошибка, созданный DTO = null", null);
					}
				} catch (NoSuchMethodException | 
						SecurityException | 
						DTOException | 
						InstantiationException | 
						IllegalAccessException | 
						IllegalArgumentException | 
						InvocationTargetException e) {
					throw new DTOException("Невозможно создать объект DTO в fromResultList.", e);
				} 
			}
			
			resultList = localDTO;
		}
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

	public void delete(boolean hardDelete) throws DTOException {
		if (mAccessChecker == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!hardDelete) {
			setDataField("mDeleteFlag", "getDeleteFlag", "setDeleteFlag", true, true);
		} else {
			// TODO Жесткое удаление из базы навсегда!			
		}
	}
	
	public void recover() throws DTOException {
		setDataField("mDeleteFlag", "getDeleteFlag", "setDeleteFlag", false, true);
	}
	
	/**
	 * Получить данные о том, есть ли изменения в буфере и отличаются ли
	 * они от оригинала. А также, какие именно поля изменены.
	 * 
	 * @return
	 * @throws DTOException 
	 */
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
	public static void applyChanges(CommonObject data, CommonDTO dto) throws DTOException {
		if (dto.mAccessChecker == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!dto.mAccessChecker.checkSave(dto)) {
			throw new DTOAccessException("У вас нет права на сохранение этого элемента.", null);
		}

		int changesCount = dto.getChangesBuffer().keySet().size();
		for (int i = 0; i < changesCount; i++) {
			String key = (String) dto.getChangesBuffer().keySet().toArray()[i];
			Object[] values = dto.getChangesBuffer().get(key);
			if (values[1] == null) {
				System.out.println("Поле сконфигурировано только для чтения т.к. сеттер не задан, пропускаем.");
			} else {
				try {
					Method setValueMethod;
					if (values[2] == null) {
						System.out.println("Вызываем "+values[1].toString()+" для сохранения пустого значения.");
					} else {
						System.out.println("Вызываем "+values[1].toString()+" для сохранения "+values[2].toString());					
					}
					Class<?> fieldClass = data.getClass().getMethod(values[0].toString()).getReturnType();
					if ((values[2] != null) && (values[2].getClass().equals(EditorDTO.class))) {
						// Если в значение нам передано DTO, то надо из него получить данные, и объект
						// посетить как свойство.
						for (Method method : data.getClass().getMethods()) {
							if (values[1].toString().equals(method.getName())) {
								if (method.getParameterTypes()[0].isAssignableFrom(((EditorDTO)values[2]).mData.getClass())) {
									setValueMethod = method;
									setValueMethod.invoke(data, values[2] != null ? (((EditorDTO)values[2]).mData) : fieldClass);
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
								if (method.getParameterTypes()[0].isAssignableFrom(values[2] != null ? values[2].getClass() : fieldClass)) {
									method.invoke(data, values[2]);
									methodInvoked = true;
									break;
								}
							}
						}
						if (!methodInvoked) throw new NoSuchMethodException("Нет подходящего метода "+values[1].toString()+" для сохранения данных типа "+values[2].getClass().getSimpleName());
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
	public void setDataField(String fieldName, String fieldGetterName, String fieldSetterName, Object value, boolean force) throws DTOException {
		if (mAccessChecker == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (!mAccessChecker.checkWrite(this, fieldName)) {
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
				if (CommonObject.class.isAssignableFrom(fieldClass)) {
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
	public Object getDataField(String fieldName, String fieldGetterName, String fieldSetterName, boolean checkAccess) throws DTOException {
		if (mAccessChecker == null && checkAccess) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		if (checkAccess && !mAccessChecker.checkRead(this, fieldName)) {
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
	        if (CommonObject.class.isAssignableFrom(fieldClass)) {
	        	// Нужно генерить DTO на основании объекта только тогда,
	        	// когда этот объект, то есть значение поля в ORM не null.
	        	CommonObject obj = (CommonObject) getValueMethod.invoke(mData);
	        	if (obj != null) {
	        		obj.setPersistentManagerSession(getPersistenceManagerSession());
		        	a = new EditorDTO((CommonObject)obj);
		        	((CommonDTO)a).addAccessChecker(mAccessChecker);
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
	
	public Long getCode() {
		if (mData != null)
			return ((CommonObject)mData).getCode().getId();
		else
			return (long)0;	
	}

	@Override
	public void clear() {
		clearChangesBuffer();
		mData = null;
	}

	public void forceUpdateField(String field, CommonDTO dto) {
		((CommonObject)mData).forceUpdateField(field, (CommonObject)dto.mData);
	}
	
	@Override
	public void refresh() {
		getPersistenceManagerSession().getEm().refresh(mData);
	}
}

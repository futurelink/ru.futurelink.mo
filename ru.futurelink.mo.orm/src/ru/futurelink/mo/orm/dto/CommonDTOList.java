/**
 * 
 */
package ru.futurelink.mo.orm.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.futurelink.mo.orm.PersistentManager;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * Структурированный список объектов DTO. Этот класс - абстракция, созданная
 * для того, чтобы обеспечить работу со списком и его упорядожение.
 * 
 * Особенность этого класса в том, что он обеспечивает порядок элементов при
 * помощи дополнительной структуры TreeSet. Эту структуру можно получить методом
 * getOrderList(). Ключ структуры - индекс, по которому просходит упорядочение,
 * значение ID элемента данных.
 * 
 * @author pavlov
 *
 */
public abstract class CommonDTOList<T extends IDTO> {
	private TreeMap<Integer, String>	mOrderList;	// Упорядочение
	private HashMap<String, T>			mDTOList;	// Данные
	
	private IDTOAccessChecker			mAccessChecker;
	private PersistentManager			mPersistent;
	
	private Class<T>					mDTOClass;
	private Integer						mIndex;		// Счетчик-индекс
	
	/**
	 * 
	 */
	public CommonDTOList(PersistentManager persistent, IDTOAccessChecker accessChecker, Class<T> DTOclass) {
		mPersistent = persistent;
		mAccessChecker = accessChecker;
		mDTOClass = DTOclass;

		mOrderList = new TreeMap<Integer, String>();
		mDTOList = new HashMap<String, T>();
		mIndex = 0;
	}
	
	/**
	 * Получить данные из структуры списка.
	 * 
	 * @return
	 */
	public Map<String, T> getDTOList() {
		return mDTOList;
	}

	/**
	 * Получить упорядочивающую структуру.
	 * 
	 * @return
	 */
	public Map<Integer, String> getOrderList() {
		return mOrderList;
	}
	
	/**
	 * Получить элемента данных по индексу из упорядочивающей
	 * карты.
	 * 
	 * @param index
	 * @return
	 */
	public T getDTOItem(Integer index) {
		return mDTOList.get(mOrderList.get(index));
	}

	/**
	 * Добавить элемент данных в список.
	 * 
	 * @param id
	 * @param dtoItem
	 * @throws DTOException 
	 */
	@SuppressWarnings("unchecked")
	public void addDTOItem(IDTO dtoItem) throws DTOException {
		// Добавляем только новые элементы, если элемент уже есть,
		// его дублировать не надо.
		if (!mDTOList.containsValue(dtoItem)) {
			String id = dtoItem.getId();
			// Если ID есть, то добавляем элемент с его ID, иначе,
			// добавляем элемент с временной ID, которая создается на базе
			// внутренненго индекса.
			if ((id == null) || id.isEmpty()) { id = "ID_"+String.valueOf(mIndex); }
			mOrderList.put(mIndex, id);
			mDTOList.put(id, (T) dtoItem);
			mIndex++;
		}
	}

	/**
	 * Удалить элемент из списка.
	 * 
	 * @param dtoItem
	 * @throws DTOException
	 */
	public void removeDTOItem(T dtoItem) throws DTOException {
		for (Integer index : mOrderList.keySet()) {
			if (mOrderList.get(index).equals(dtoItem.getId())) {
				mOrderList.remove(index);
				mDTOList.remove(dtoItem.getId());
				return;
			}
		}
	}
	
	/**
	 * Очистить структуры списка.
	 */
	public void clear() {
		mOrderList.clear();
		mDTOList.clear();
		mIndex = 0;
	}
	
	/**
	 * @return
	 */
	protected IDTOAccessChecker getAccessChecker() {
		return mAccessChecker;
	}

	/**
	 * @return
	 */
	protected Class<T> getDTOClass() {
		return mDTOClass;
	}

	/**
	 * @return
	 */
	protected PersistentManager getPersistent() {
		return mPersistent;
	}
}

/**
 * 
 */
package ru.futurelink.mo.orm.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.PersistentManagerSession;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public class EditorDTOList<T extends CommonDTO> 
	extends CommonDTOList<T> {

	/**
	 * @param persistent
	 * @param accessChecker
	 * @param DTOclass
	 */
	public EditorDTOList(PersistentManagerSession persistent,
			IDTOAccessChecker accessChecker, Class<T> DTOclass) {
		super(persistent, accessChecker, DTOclass);
	}

	/**
	 * Заполнить список элементами CommonObject.
	 * 
	 * @param sourceList
	 * @throws DTOException
	 */
	public void addObjectList(List<? extends CommonObject> sourceList) throws DTOException {
		if ((sourceList == null) || (sourceList.size() == 0)) {
			clear();
			return;
		} else {
			for (CommonObject object : sourceList) {
				object.setPersistentManagerSession(getPersistentManagerSession());
				try {
					Constructor<T> ctr = getDTOClass().getConstructor(CommonObject.class);
					T dto = ctr.newInstance(object);
					if (dto != null) {
						if (dto.getAccessChecker() == null) dto.addAccessChecker(getAccessChecker());
						addDTOItem(dto);						
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
					throw new DTOException("Невозможно заполнить объект списка DTO.", e);
				} 
			}		
		}		
	}
}

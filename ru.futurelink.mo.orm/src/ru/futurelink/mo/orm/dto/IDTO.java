/**
 * 
 */
package ru.futurelink.mo.orm.dto;

import java.util.ArrayList;
import java.util.HashMap;

import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;

/**
 * @author pavlov
 *
 */
public interface IDTO {
	/**
	 * Получает данные из модели ОРМ, путем вызова геттера, имя которого
	 * передается в метод.
	 * 
	 * @param fieldGetterName имя геттера в модели данных
	 * @param fieldSetterName имя сеттера в модели данных
	 * @return
	 * @throws DTOException
	 */	
	public Object getDataField(String fieldName, String fieldGetterName, 
			String fieldSetterName) throws DTOException;
	
	/**
	 * Сохраняет данные в модели DTO определенным образом, в зависимости от реализации.
	 * 
	 * @param fieldGetterName
	 * @param fieldSetterName
	 * @param value
	 * @throws DTOException
	 */
	public void setDataField(String fieldName, String fieldGetterName, 
			String fieldSetterName, Object value) throws DTOException;

	/**
	 * Очистить буфер изменений DTO, откатить изменения.
	 * 
	 * @return
	 */
	public void clearChangesBuffer();

	/**
	 * Получить буфер изменений.
	 * 
	 * @return
	 */
	public HashMap<String, Object[]> getChangesBuffer();
	
	/**
	 * Очистить DTO.
	 */
	public void clear();
	
	/**
	 * Сохранить DTO.
	 * 
	 * @throws DTOException
	 * @throws SaveException
	 */
	public void save() throws DTOException, SaveException;
	
	/**
	 * Получить ID данных, присоединенных к DTO.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public String getId() throws DTOException;
	
	/**
	 * Получить пометку удаления данных.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public boolean getDeleteFlag() throws DTOException;
	
	/**
	 * Установить пометку удаления данных.
	 * 
	 * @param deleteFlag
	 * @throws DTOException
	 */
	public void setDeleteFlag(boolean deleteFlag) throws DTOException;	
	
	/**
	 * Получить пользователя, создателя данных.
	 * @return
	 */
	public User getCreator();
	
	/**
	 * Получить список изменений данных.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public ArrayList<String> getChangedData() throws DTOException;


	/**
	 * Установить для DTO агента, проверяющего права доступа.
	 * 
	 * @param checker
	 */
	public void addAccessChecker(IDTOAccessChecker checker);
	
	/**
	 * Получить агент, проверяющий права доступа.
	 * 
	 * @return
	 */
	public IDTOAccessChecker getAccessChecker();	
}

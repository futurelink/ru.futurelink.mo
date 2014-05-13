/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface IListController
	extends IController
{
	/**
	 * Обработка запроса данных для списка.
	 */
	public void handleDataQuery() throws DTOException;

	/**
	 * Данные получены.
	 * @throws DTOException 
	 */
	public void handleDataQueryExecuted() throws DTOException;
	
	/**
	 * Выбрать элемент списка.
	 * 
	 * @param data
	 */
	public void setActiveData(CommonDTO data);

	/**
	 * Получить выбранный элемент.
	 * 
	 * @return
	 */
	public CommonDTO getActiveData();
}

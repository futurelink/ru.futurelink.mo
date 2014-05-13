/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public interface IListEditController extends IListController {
	/**
	 * Обработка создания нового элемента списка.
	 * @throws DTOException 
	 * @throws InitException 
	 */
	public void handleCreate() throws DTOException, InitException;
	
	/**
	 * Обработка удаления элемента.
	 * @throws DTOException 
	 */
	public void handleDelete() throws DTOException;
	
	/**
	 * Обработка восстановления элемента.
	 * 
	 * @throws DTOException
	 */
	public void handleRecover() throws DTOException;	
	
	/**
	 * Обработка редактирования элемента
	 * @throws DTOException 
	 */
	public void handleEdit() throws DTOException;
}

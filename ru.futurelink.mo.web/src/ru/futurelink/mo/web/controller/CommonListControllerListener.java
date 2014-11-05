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

package ru.futurelink.mo.web.controller;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.controller.iface.ListDTOAccessor;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Обработчик событий типичного списка.
 * 
 * @author Futurelink
 *
 */
public interface CommonListControllerListener extends CommonControllerListener, ListDTOAccessor {
	/**
	 * Событие происходит обычно по функции создания элемента. Если
	 * обработчик создается и привязывается к контроллеру, а метод вызывается
	 * из композита то это, как правило, метод создания нового элемента.
	 * Обычно из него надо вызвать handleCreate().
	 * @throws InitException 
	 * @throws DTOException 
	 */
	public void create() throws DTOException, InitException;
	
	/**
	 * Событие происходит по функции режактирования элмента. Если
	 * это - метод обработчика, который вызывается из композится, то
	 * скорее всего это обработка пользовательской функции редактирования.
	 * В этом случае обычно вызывается handleEdit().
	 * @throws DTOException 
	 */
	public void edit() throws DTOException;
	
	/**
	 * Событие происходит по функции удаления элемента. Если
	 * это - метод обработчика, который вызывается из композита, то
	 * скорее всего  это обработка пользовательской функции удаления.
	 * В этом случае обычно вызывается handleDelete(). 
	 * @throws DTOException 
	 */
	public void delete() throws DTOException;
	
	/**
	 * Обработка выбора элемента списка.
	 * При реализации списка, унаследованного от CommonList нужно реализовать
	 * вызов этого метода обработчика в композите во всех случаях, когда
	 * пользователь выбирает элемент в списке, а может быть и в каких-то других. 
	 * 
	 * @param data
	 */
	public void itemSelected(CommonDTO data);
	
	/**
	 * То же что itemSelected, но для двойного клика.
	 * 
	 * @param data
	 */
	public void itemDoubleClicked(CommonDTO data);
	
	/**
	 * Обработка изменения фильтра списка.
	 */
	public void filterChanged();
}

/**
 * 
 */
package ru.futurelink.mo.web.composites.table;

import org.eclipse.jface.viewers.IContentProvider;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface ICommonTable {
	public void setInput(CommonDTOList<? extends CommonDTO> data);
	public Object getInput();
	
	public void setContentProvider(IContentProvider provider);
	public void setRowHeight(Integer height);
	
	public void selectById(String id) throws DTOException;
	public void selectByDTO(CommonDTO dto);
	
	public void addTableListener(CommonTableListener listener);
	public void initTable();
	
	/**
	 * Создание колонок таблицы. Удобно использовать метод addColumn(..) для добавления
	 * колонок.
	 */
	public void createTableColumns();	
}

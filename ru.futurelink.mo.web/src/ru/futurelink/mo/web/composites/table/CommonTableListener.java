package ru.futurelink.mo.web.composites.table;

import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

public interface CommonTableListener {
	public void itemSelected(IDTO data) throws DTOException;
	public void itemDoubleClicked(IDTO data);
	public void onColumnAdded(TableColumn column, String filterField, String filterFieldGetter, 
			String filterFieldSetter, Class<?> filterFieldType);
	public void onColumnResized(TableColumn column);
}

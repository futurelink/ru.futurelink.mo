package ru.futurelink.mo.web.composites.table;

import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

public interface CommonTableListener {
	public void itemSelected(CommonDTO data) throws DTOException;
	public void itemDoubleClicked(CommonDTO data);
	public void onColumnAdded(TableColumn column, String filterField, String filterFieldGetter, String filterFieldSetter, Class<?> filterFieldType);
	public void onColumnResized(TableColumn column);
}

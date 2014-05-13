/**
 * 
 */
package ru.futurelink.mo.web.controller;

import org.eclipse.swt.widgets.TableColumn;

/**
 * @author pavlov
 *
 */
public interface CommonTableControllerListener {
	/**
	 * Обработка изменения размера колонки.
	 * 
	 * @param column
	 */
	public void onColumnResized(TableColumn column);
	
	/**
	 * Обработка добавления колонки в таблицу.
	 * 
	 * @param column
	 */
	public void onColumnAdded(TableColumn column, String filterField, String filterFieldGetter, String filterFieldSetter, Class<?> filterFieldType);
}

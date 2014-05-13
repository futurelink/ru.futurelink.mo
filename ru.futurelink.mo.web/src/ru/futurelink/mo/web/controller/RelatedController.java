/**
 * 
 */
package ru.futurelink.mo.web.controller;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface RelatedController {
	enum SaveMode { SAVE_AFTER, SAVE_BEFORE };

	public void		refresh(boolean refreshSubControllers) throws DTOException;
	public void		save() throws DTOException, SaveException;
	public boolean		getChanged();
	public SaveMode		getSaveMode();
}

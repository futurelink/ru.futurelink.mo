package ru.futurelink.mo.orm;

import ru.futurelink.mo.orm.exceptions.SaveException;

public interface SaveHandler {
	public Object onBeforeSave() throws SaveException;
	public Object onAfterSave() throws SaveException;
}

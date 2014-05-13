package ru.futurelink.mo.web.controller;

import ru.futurelink.mo.web.composites.fields.datapicker.DataPicker;

public interface CommonItemEditControllerListener extends CommonItemControllerListener {
	public void saveButtonClicked();
	public void cancelButtonClicked();	
	public void openDataPickerDialog(DataPicker picker);
}

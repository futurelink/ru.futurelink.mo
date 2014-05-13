package ru.futurelink.mo.web.composites.fields.datapicker;

import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.controller.CommonTableControllerListener;

public interface DataPickerControllerListener 
	extends CommonListControllerListener, CommonTableControllerListener {
	public void ok();
	public void cancel();
}

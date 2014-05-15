package ru.futurelink.mo.web.recycle;

import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.controller.CommonTableControllerListener;

public interface RecycleListControllerListener extends CommonListControllerListener, CommonTableControllerListener {
	public void recover();
}

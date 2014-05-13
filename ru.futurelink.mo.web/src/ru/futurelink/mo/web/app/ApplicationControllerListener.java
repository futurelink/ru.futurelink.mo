package ru.futurelink.mo.web.app;

import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;

public interface ApplicationControllerListener extends CommonControllerListener {
	public CompositeController runUsecase(String usecaseName, Class<?> dataClass);
	public void refresh() throws Exception;	
}

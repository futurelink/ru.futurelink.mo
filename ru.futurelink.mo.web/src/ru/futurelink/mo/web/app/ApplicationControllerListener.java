package ru.futurelink.mo.web.app;

import java.util.Map;

import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;

/**
 * Application controller listener interface.
 *
 * This listener is to use with ApplicationController as controller listener.
 * 
 * @author pavlov
 *
 */
public interface ApplicationControllerListener extends CommonControllerListener {
	public CompositeController runUsecase(String usecaseName, Class<?> dataClass);
	public void refresh() throws Exception;
	public void navigate(String tag, Map<String, Object> params);
}

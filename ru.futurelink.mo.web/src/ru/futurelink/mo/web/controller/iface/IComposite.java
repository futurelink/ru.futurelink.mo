/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import java.util.Locale;
import java.util.ResourceBundle;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public interface IComposite {
	public void init() throws InitException;
	
	public void addControllerListener(CommonControllerListener listener);
	public CommonControllerListener getControllerListener();
	
	public void setOwnerDialog(CommonDialog dialog);
		
	public void setStrings(ResourceBundle bundle);
	public Locale getLocale();
	public String getLocaleString(String stringName);
	public String getLocaleNumeric(Integer value, String single, String multiple);
	
	public ApplicationSession getSession();	
}

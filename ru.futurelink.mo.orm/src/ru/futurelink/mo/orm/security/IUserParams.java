/**
 * 
 */
package ru.futurelink.mo.orm.security;

import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface IUserParams {
	public void setUsecaseName(String usecaseName);
	public String getUsecaseName();
	
	public void setParamName(String paramName);
	public String getParamName();
	
	public void setUser(String userId);
	public String getUser();

	public void setParam(String name, Object value);
	public Object getParam(String name);

	public Object save() throws SaveException;
}

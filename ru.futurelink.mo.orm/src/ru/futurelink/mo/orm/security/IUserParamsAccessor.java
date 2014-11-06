/**
 * 
 */
package ru.futurelink.mo.orm.security;

import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface IUserParamsAccessor {
	public IUserParams	getUserParams(String userId, String usecaseName, String paramName);
	public void		saveUserParams(IUserParams params) throws SaveException;
}

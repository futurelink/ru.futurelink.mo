/**
 * 
 */
package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.security.User;

/**
 * Агент проверки. Разрешает доступ только к своим обьектам, если владелец-создатель
 * объекта не равен пользователю системы - то права не даются.
 * 
 * Объекты с пустым создателем считаются доступными всем, права на них даются всегда.
 * 
 * @author pavlov
 *
 */
public class AllowOwnChecker implements IDTOAccessChecker {

	private User mUser;
	
	public AllowOwnChecker(User user) {
		mUser = user;
	}
	
	public User getUser() { return mUser; }
	
	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {
		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {
		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkSave(CommonDTO dto) {
		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean checkCreate(CommonDTO dto) {
		if (dto != null && dto.getCreator() != null) {
			if (dto.getCreator().getId().equals(mUser.getId())) return true;
			return false;
		}
		return true;
	}

}

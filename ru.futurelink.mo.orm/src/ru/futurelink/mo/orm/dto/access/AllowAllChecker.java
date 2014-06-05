/**
 * 
 */
package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;

/**
 * @author pavlov
 *
 */
public class AllowAllChecker implements IDTOAccessChecker {

	@Override
	public boolean checkCreate(CommonDTO dto) {
		return true;
	}

	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {
		return true;
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {
		return true;
	}

	@Override
	public boolean checkSave(CommonDTO dto) {
		return true;
	}

}

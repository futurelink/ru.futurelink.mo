/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface ListDTOAccessor {
	public CommonDTOList<? extends CommonDTO> getControllerDTO() throws DTOException;
}

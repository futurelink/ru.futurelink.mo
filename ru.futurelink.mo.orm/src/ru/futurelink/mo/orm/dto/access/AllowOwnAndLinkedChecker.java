/**
 * 
 */
package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTOWithLinkage;
import ru.futurelink.mo.orm.security.User;

/**
 * @author pavlov
 *
 */
public class AllowOwnAndLinkedChecker extends AllowOwnChecker {

	public AllowOwnAndLinkedChecker(User user) {
		super(user);
	}
	
	@Override
	public boolean checkRead(CommonDTO dto, String fieldName) {

		if (dto != null && 	EditorDTOWithLinkage.class.isAssignableFrom(dto.getClass())) {			
			if (((EditorDTOWithLinkage)dto).getLinkageCreator().getId().equals(getUser().getId())) return true;
		}
		
		return super.checkRead(dto, fieldName);
	}

	@Override
	public boolean checkWrite(CommonDTO dto, String fieldName) {

		if (dto != null && 	EditorDTOWithLinkage.class.isAssignableFrom(dto.getClass())) {			
			if (((EditorDTOWithLinkage)dto).getLinkageCreator().getId().equals(getUser().getId())) return true;
		}

		return super.checkWrite(dto, fieldName);
	}
	
}

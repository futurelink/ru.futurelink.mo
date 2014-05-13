/**
 * 
 */
package ru.futurelink.mo.web.recycle;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.composites.table.CommonTableContentProvider;

/**
 * Провайдер для корзины - показывает элементы с deleteFlag > 0, в отличие от
 * обычного провайдера.  
 * 
 * @author pavlov
 *
 */
public class RecycleTableContentProvider extends CommonTableContentProvider {
	private static final long serialVersionUID = 1L;

	@Override
	protected boolean getIsDisplayable(CommonDTO element) throws DTOException {
		return true;
	}
	
}

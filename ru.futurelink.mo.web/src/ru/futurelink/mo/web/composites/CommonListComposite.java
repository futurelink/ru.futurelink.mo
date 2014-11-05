package ru.futurelink.mo.web.composites;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.IListComposite;
import ru.futurelink.mo.web.controller.iface.ListDTOAccessor;

/**
 * <p>Composite to work with listed data sets.</p>
 *
 * <p>This composite has CommonDTOList object which contains a set of DTO objects, each of them is an
 * item of the data list to deal with.</p>
 *
 * @author pavlov
 * @param <T>
 *
 */
abstract public class CommonListComposite 
	extends CommonDataComposite
	implements IListComposite {

	private static final long serialVersionUID = 1L;
	
	protected CommonDTOList<? extends CommonDTO>	mDTO;
	private CommonDTO								mActiveData;
	
	public CommonListComposite(ApplicationSession session,
			Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);

        // Create default editor DTO list with default access checker
		mDTO = new EditorDTOList<CommonDTO>(getSession().persistent(),
            new AllowOwnChecker(getSession().getUser()), CommonDTO.class
        );
	}

	/**
	 * Link CommonDTOList object to this composite.
	 * 
	 * @param data коллекция CommonDTO
	 * @throws DTOException 
	 */
	protected void attachDTO(CommonDTOList<? extends CommonDTO> data) throws DTOException {
		mDTO = data;				
		refresh();
	}

	/**
	 * Unlink and free CommonDTOList linked to composite.
	 * @throws DTOException 
	 */
	protected void removeDTO() throws DTOException {
		if (mDTO != null) {		
			mDTO = null;			
			refresh();
		}
	}
	
	/**
     * <p>Get DTO list:</p>
     * <ul>
     * <li>1) attached to list view if controller listener is not assigned</li>
     * <li>2) attached to list controller if controller listener is assigned via its
     *    getControllerDTO() method. Controller listener must implement ListDTOAccessor
     *    interface.</li>
     * </ul>
     *
	 * @return - CommonDTOList instance
	 */
	@Override
	public CommonDTOList<? extends CommonDTO> getDTO() throws DTOException {
		if (getControllerListener() != null) {
			
			// Если у нас обработчик контроллера не кастуется в обработчик контроллера списка,
			// то надо обработать это и вывалить эксепшн.
			if (!(ListDTOAccessor.class.isAssignableFrom(getControllerListener().getClass()))) {
				throw new DTOException("Неправильный обработчик списка на контроллере списка. "
						+ "Обработчик контроллера должен унаследовать "
						+ "ListDTOAccessor и метод getControllerDTO()", null);
			}

			if (((ListDTOAccessor)getControllerListener()).getControllerDTO() != null) {
				return ((ListDTOAccessor)getControllerListener()).getControllerDTO();
			}
		}

		return mDTO;
	}

	/**
	 * Get FilterDTO object which desribes filter conditions to be applied to list view.
	 * 
	 * @return объект FilterDTO
	 * @throws DTOException
	 */
	@Override
	public FilterDTO getFilter() throws DTOException {
		if (getParam("filter") != null)
			return (FilterDTO)getParam("filter");
		else
			throw new DTOException("Нет объекта фильтра переданного из контроллера!", null);
	}

	/**
	 * Set active (selected) item in list view.
	 *
	 * @param data element from CommonDTOList attached to list view
	 */
	@Override
	public void setActiveData(CommonDTO data) {
		mActiveData = data;
	}

	/**
	 * Get active (selected) item from list view.
	 * 
	 * @return
	 */
	@Override
	public CommonDTO getActiveData() {
		return mActiveData;
	}

}

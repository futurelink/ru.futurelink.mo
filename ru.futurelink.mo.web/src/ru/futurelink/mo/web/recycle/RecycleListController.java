package ru.futurelink.mo.web.recycle;

import java.util.List;

import javax.persistence.TypedQuery;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.ViewerDTO;
import ru.futurelink.mo.orm.dto.ViewerDTOList;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.SimpleListController;

/**
 * Контроллер корзины.
 * 
 * @author pavlov
 *
 */
public class RecycleListController extends SimpleListController {

	private ViewerDTOList<ViewerDTO> mList;

	public RecycleListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleDataQuery() throws DTOException {
		mList = new ViewerDTOList<ViewerDTO>(new AllowOwnChecker(getSession().getUser()), ViewerDTO.class);
		
		// Выполняем именованый запрос всех затратных документов нашего пользователя
		logger().debug("Запрос данных корзины...");
		TypedQuery<?> q2 = mSession.persistent().getEm().createQuery(
				"SELECT d FROM "+getDataClass().getSimpleName()+" d " +
				"where d.mCreator = :creator and d.mDeleteFlag = 1 and d.mOutdated in (0, null) order by d.mId desc", 
				getDataClass());
		q2.setParameter("creator", getSession().getDatabaseUser());
		if (q2.getResultList().size() > 0) {			
			logger().debug("Количество элементов: {}", q2.getResultList().size());

			try {
				mList.addObjectList((List<? extends CommonObject>) q2.getResultList());
				setDTO(mList);
			} catch (DTOException e) {
				throw new DTOException("Ошибка открытия корзины: "+e.getMessage(), e);
			}					
		}
	}

	@Override
	protected void doBeforeCreateComposite() {
		params().add("listComposite", RecycleListComposite.class);
		params().add("itemDialogTitle", "recycle");

		// А табличка tableClass для списка корзинки передается из
		// контроллера того списка, который вызывает окно корзинки - 
		// она там уже проставлена. Таким образом в корзинке используется
		// то же отображение, что и в основном списке.
	}

	/* (non-Javadoc)
	 * @see ru.futurelink.mo.web.controller.SimpleListController#doAfterCreateComposite()
	 */
	@Override
	protected void doAfterCreateComposite() {		
		super.doAfterCreateComposite();
		
		// Задаем провайдер контента для таблицы, чтобы отображала элементы
		// с deleteFlag > 0.
		((RecycleListComposite)getComposite()).setTableContentProvider(new RecycleTableContentProvider());
	}
	
	/**
	 * Хоть список и стандартный, но в нем отключены все стандартные функции, такие
	 * как создание, правка, удаление элемента. В общем-то, это логично, нечего вообще
	 * менять в корзине удаленных элементов. Зато можно восстановить элемент, а для этого
	 * добавлена соответствующая функция.
	 */
	@Override
	public CommonControllerListener createControllerListener() {
		return new RecycleListControllerListener() {
			
			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}

			@Override
			public void recover() {
				// Восстановление элемента из удаленных
			}

			@Override
			public void create() {}

			@Override
			public void edit() {}

			@Override
			public void delete() {}

			@Override
			public void itemSelected(CommonDTO data) {}

			@Override
			public void itemDoubleClicked(CommonDTO data) {}

			@Override
			public void filterChanged() {}

			@Override
			public CommonDTOList<? extends CommonDTO> getControllerDTO() throws DTOException {
				return getDTO();
			}
		};
	}

	@Override
	public void handleDataQueryExecuted() {}
}

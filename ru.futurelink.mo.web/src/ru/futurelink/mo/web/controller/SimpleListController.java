package ru.futurelink.mo.web.controller;

import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.dialogs.CommonItemDialog;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListEditController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Простой контроллер для композита SimpleListComposite. Он по-умолчанию использует
 * SimpleListComposite для отображения данных и его поведение определяется именно
 * этим композитом.
 * 
 * Нужно переопределить его, переопределить handleDataQuery для получения данных,
 * в методе doBeforeCreateComposite:
 * 
 * 		params().add("tableClass", ...); // Класс таблицы для отображения
 *		params().add("itemControllerClass", ...); // Класс контроллера для отображения элемента
 *		params().add("itemDialogTitle", ...); // Заголовок окна редактирования элемента
 * 
 * @author pavlov
 * @since 1.2
 *
 */
abstract public class SimpleListController 
	extends CommonListController
	implements IListEditController {

	public enum EditMode { DIALOG, CONTAINER };
	
	public SimpleListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
	}

	public SimpleListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container, CompositeParams compositeParams) { 
		super(parentController, dataClass, container, compositeParams);
	}
	
	/**
	 * Handler for item double click event.<be/>
	 * <br/>
	 * This handler works with data item got by getActiveData(),
	 * note the @data parameter is not the same as getActiveData(),
	 * the method can be overridden in child classes to work with other
	 * data item then selected in list, but @data always contains selected
	 * data item DTO. 
	 * 
	 * @param data
	 */
	public void handleItemDoubleClicked(CommonDTO data) {
		if (data != null) {
			if (params().get("itemEditMode") != EditMode.CONTAINER) {
				if (openEditDialog((EditorDTO)getActiveData()) != null) {
					// Обновить список, перечитать данные
					try {
						handleDataQuery();
					} catch (DTOException ex) {
						handleError("невозможно обновить список.", ex);
					}
				}
			} else {
				openEdit(getParentController(), (EditorDTO)data);
			}
		}
	}
	
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {
		IDragDropDecorator tbl = (IDragDropDecorator)((SimpleListComposite)getComposite()).getTable();
		if (tbl != null)
			tbl.addDragSupport(operations, transferTypes, listener);
	}
	
	// Обработка создания новго элемента
	@Override
	public void handleCreate() {
		if (params().get("itemEditMode") != EditMode.CONTAINER) {
			if (openEditDialog(null) != null) {	
				// Обновить список, перечитать данные
				try {
					handleDataQuery();
				} catch (DTOException ex) {
					handleError("невозможно обновить список.", ex);
				}
			}
		} else {
			// Запустить контроллер редактирования в главном контейнере, как
			// юзкейс, а после закрытия этого контроллера запустить юзкейс,
			// который был там раньше
			openEdit(getParentController(), null);
		}
	}
	
	// Обработка удаления элемента
	@Override
	public void handleDelete() {
		String id = "";
		// Только если есть выбранный элемент, есть активные данные
		if (getActiveData() != null) {
			try {
				id = getActiveData().
						getDataField("mId", "getId", "setId").toString();
				CommonObject obj = getSession().persistent().open(getDataClass(), id);
				obj.delete();
				obj.save();
				logger().debug("Удален элемент ID: ", id);
			} catch (DTOException ex) {
				handleError("Ошибка доступа DTO при удалении элемента.", ex);
			} catch (OpenException ex) {
				handleError("Ошибка открытия при удалении элемента.", ex);
			} catch (SaveException ex) {
				handleError("Ошибка сохранения при удалении элемента.", ex);
			}

			// Обновить список, перечитать данные
			try {
				handleDataQuery();
			} catch (DTOException ex) {
				handleError("невозможно обновить список.", ex);
			}			
		}
	}

	// Обработка редактирования элемента
	@Override
	public void handleEdit() {
		handleItemDoubleClicked(getActiveData());
	}

	@Override
	protected CommonComposite createComposite(CompositeParams compositeParams) {
		// Добавлем в параметры объект FilterDTO
		params().add("filter", getFilter());
		
		@SuppressWarnings("unchecked")
		Class<? extends CommonListComposite> listCompositeClass = 
				(Class<? extends CommonListComposite>)params().get("listComposite");
		if (listCompositeClass != null) {
			SimpleListComposite composite = null;
			try {
				Constructor<?> constr = listCompositeClass.getConstructor(ApplicationSession.class,
						Composite.class, int.class, CompositeParams.class);
				composite = (SimpleListComposite) constr.newInstance(
						getSession(), getContainer(), SWT.NONE, params());
			} catch (Exception ex) {
				logger().error("Не удалось создать окно композита для контроллера", ex);
			}
			return composite;
		} else {
			return new SimpleListComposite(getSession(), getContainer(), SWT.NONE, params());
		}
	}

	@Override
	public void handleRecover() throws DTOException {
		
	}

	@Override
	protected void doAfterCreateComposite() {
		// Зацепляем обработчик контроллера на композит
		getComposite().addControllerListener(createControllerListener());
		
		// Запускаем создание колонок таблицы на композите
		((SimpleListComposite)getComposite()).createTableColumns();
		
		try {
			handleDataQuery();
		} catch (DTOException ex) {
			handleError("невозможно обновить список. "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * Открывает окно редактирования выбранного элмента данных.
	 * 
	 * @param data
	 * @return
	 */
	protected Object openEditDialog(EditorDTO data) {
		return new CommonItemDialog(getSession(), getComposite(), this, params()).open(data);
	}
	
	/**
	 * Open item edit or creation as subconroller.
	 * 
	 * @param parentController
	 * @param data
	 * @return
	 */
	private Object openEdit(ICompositeController parentController, EditorDTO data) {
		Composite container = (Composite) params().get("itemEditContainer");
		
		if (parentController != null)
			parentController.clear();

		CommonItemController ctrl = createItemController(parentController, container, new CompositeParams());

		try {
			if (parentController != null)
				parentController.addSubController(ctrl);
		} catch (InitException ex) {
			ctrl.handleError("Ошибка инициализации контроллера.", ex);
			return null;
		}

		// Initialize controller
		try {
			ctrl.init();
		} catch (InitException ex1) {
			ctrl.handleError("Ошибка инициализации контроллера.", ex1);
			return null;
		}
		
		// Check composite is created right
		if (ctrl.getComposite() == null) {			
			ctrl.uninit();
			ctrl.handleError("Композит для отображения в диалоговом окне не создан!", null);
			return -1;
		}
		
		ctrl.getComposite().layout(true, true);

		// Create or open data
		if (data == null) {
			// Создание нового элемента
			try {
				ctrl.create();
			} catch (DTOException ex) {
				ctrl.handleError("Ошибка создания нового элемента.", ex);
				ctrl.uninit();
				return null;
			}
		} else {
			// Открыть на редактирование существующий элемент
			try {
				String id = data.getDataField("id", "getId", "setId").toString();
				ctrl.getSession().logger().debug("Открытие окна редакитрования для элмента с ID: {}", id);
				ctrl.openById(id);
			} catch (NumberFormatException ex) {
				handleError("Неверный формат ID.", ex);
				ctrl.uninit();
				return null;
			} catch (OpenException ex) {
				handleError("Ошибка открытия элемента.", ex);				
				ctrl.uninit();
				return null;
			} catch (DTOException ex) {
				handleError("Ошибка доступа в DTO.", ex);
				ctrl.uninit();
				return null;
			}
		}

		return ctrl;		
	}
	
	@Override
	public CommonControllerListener createControllerListener() {
		return new SimpleListControllerListener(this);
	}
	
	@Override
	protected void doBeforeInit() throws InitException {
		
	}
	
	@Override
	protected void doAfterInit() throws InitException {

	}
	
	@Override
	public void handleDataQueryExecuted() throws DTOException {
		((CommonListComposite)getComposite()).disableToolbar();

		setToolEnabled("create", true);

		((CommonListComposite)getComposite()).refresh();		
	}
	
	/**
	 * Метод обработки изменения размера колонки таблицы.
	 * 
	 * @param column
	 */
	protected void onTableColumnResized(TableColumn column) {}
	
	/**
	 * Метод обрабаотки создания колонки в таблице.
	 * 
	 * @param column
	 */
	protected void onTableColumnAdded(TableColumn column, String columnField, 
			String columnFieldGetter, String columnFieldSetter, Class<?> columnFieldType) {}
}

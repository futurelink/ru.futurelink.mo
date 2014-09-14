package ru.futurelink.mo.web.composites.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.mongodb.objects.UserParams;

/**
 * Класс CommonTable является оберткой над JFace TableViewer. Снабженный только теми методами,
 * которые используются в этой системе он несколько облегчает работу с таблицами.
 * 
 * @author Futurelink
 *
 */
public abstract class CommonTable 
	extends CommonComposite 
	implements IDragDropDecorator, ICommonTable 
{
	private static final long serialVersionUID = 1L;

	public		CommonComposite							mTableTop;
	public		CommonComposite							mTableBottom;
	protected 	TableViewer								mTableViewer;
	private 	IContentProvider 						mContentProvider;
	private 	CommonDTOList<? extends CommonDTO>	mData;

	private		CommonTableListener 					mListener;

	// Пользовтельские параметры таблицы
	private		UserParams								mUserParams;

	public CommonTable(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);

		/*
		 * Верхняя линейка-контейнер
		 */
		GridData gdTop = new GridData(GridData.FILL_HORIZONTAL);
		gdTop.heightHint = 1;
		mTableTop = new CommonComposite(getSession(), this, SWT.NONE, null);
		mTableTop.setLayoutData(gdTop);

		// Получим пользовательские параметры. Если указан юзкейс, то грузим параметры для него,
		// если не указан - грузим параметры с пустым юзкейсом.
		String usecaseName = ((params != null) && (params.get("usecaseName") != null)) ? params.get("usecaseName").toString() : "";		
		mUserParams = getSession().getUserParams(usecaseName, getClass().getSimpleName());

		mTableViewer = new TableViewer(this, SWT.NONE);
		mTableViewer.getTable().setHeaderVisible(true);
		mTableViewer.getTable().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		mTableViewer.getTable().setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		/*
		 * Нимняя линейка-контейнер
		 */
		GridData gdBottom = new GridData(GridData.FILL_HORIZONTAL);
		gdBottom.heightHint = 1;		
		mTableBottom = new CommonComposite(getSession(), this, SWT.NONE, null);
		mTableBottom.setLayoutData(gdBottom);
		
		// TODO Пока что таблица будет поддерживать только одиночное выделение
		mTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {			
			@Override
			public void selectionChanged(SelectionChangedEvent ev) {
				IStructuredSelection selection = (IStructuredSelection)ev.getSelection();
				if (!selection.isEmpty()) {
					itemSelected(selection.getFirstElement());
					try {
						if (mListener != null)
							mListener.itemSelected((CommonDTO)selection.getFirstElement());
					} catch (DTOException e) {
						// TODO Ошибка обработки выбора элемента в таблице
					}
				}
			}
		});
		
		mTableViewer.addDoubleClickListener(new IDoubleClickListener() {			
			@Override
			public void doubleClick(DoubleClickEvent ev) {
				IStructuredSelection selection = (IStructuredSelection)ev.getSelection();
				if (!selection.isEmpty()) {
					if (mListener != null)
						mListener.itemDoubleClicked((CommonDTO)selection.getFirstElement());
				}
			}
		});

		// Этот листенер добавлен для того, чтобы была возможность снять
		// выделение с элементов кликом по пустому месту в таблице
		mTableViewer.getTable().addMouseListener(new MouseListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				if (mTableViewer.getCell(new Point(arg0.x, arg0.y)) == null) {
					mTableViewer.getTable().deselectAll();
					itemSelected(null);
					try {
						if (mListener != null)
							mListener.itemSelected(null);
					} catch(DTOException e) {
						// TODO Ошибка обработки выбора элемента в таблице  
					}
				}				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});

		mContentProvider = createContentProvider();
		if (mContentProvider != null) {
			mTableViewer.setContentProvider( mContentProvider );
		}		
	}
	
	@Override
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {
		mTableViewer.addDragSupport(operations, transferTypes, listener);
	}
	
	@Override
	public void addDropSupport(int operations, Transfer[] transferTypes,
			DropTargetListener listener) {
	}

	/**
	 * Композит над таблицей для размещения элементов управления.
	 * 
	 * @return
	 */	
	public final CommonComposite getTableTop() {
		return mTableTop;
	}
	
	/**
	 * Композит под таблицей для размещения элементов управления.
	 * 
	 * @return
	 */
	public final CommonComposite getTableBottom() {
		return mTableBottom;
	}	
	
	/**
	 * Метод добваления колонки в таблицу без контент-провайдера.
	 * 
	 * @param title
	 * @param initialWidth
	 * @return
	 */
	@Deprecated
	protected TableViewerColumn addColumn(String title, Integer initialWidth) {
		return addColumn(title, initialWidth, null);
	}

	/**
	 * Метод реализованный для удобства добваления колонок в таблицу.
	 * 
	 * @param title заголовок колонки
	 * @param initialWidth начальная ширина колонки в пикселях
	 * @return объект колонки JFace TableViewerColumn
	 */
	protected TableViewerColumn addColumn(String title, Integer initialWidth, ColumnLabelProvider provider) {
		TableViewerColumn c = new TableViewerColumn(mTableViewer, SWT.NONE);
		final TableColumn column = c.getColumn();

		// Пытаемся получить параметр ширины, если получилось, то
	    // просталяем нужную нам ширину, иначе - по умолчанию.
	    if (mUserParams.getParam(title+"_size") != null) {
	    	column.setWidth((Integer)mUserParams.getParam(title+"_size"));
	    } else {
	    	column.setWidth(initialWidth);
	    }

	    // Если провайдер указан - устанавливаем его для этой колонки.
	    if (provider != null)
	    	c.setLabelProvider(provider);

	    // Но только после того, как мы проставили ширину надо привязать
	    // обработчик ресайза - чтобы не выполнять каждый раз созранение.
		column.addControlListener(new ControlListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void controlResized(ControlEvent arg0) {
				if (mListener != null)
					mListener.onColumnResized(column);

				mUserParams.setParam(column.getText()+"_size", column.getWidth());
				mUserParams.save();
			}
			
			@Override
			public void controlMoved(ControlEvent arg0) {}
		});
	    column.setText(title);	    
	    
		if (mListener != null) {			
			// Если лейбл-провайдер реализует фильтрацию, то используем метод getFilterField			
			if ((provider != null) && CommonTableFilter.class.isAssignableFrom(provider.getClass())) {
				String[] f = ((CommonTableFilter) provider).getFilterField();
				Class<?> t = ((CommonTableFilter) provider).getFilterFieldType();
				mListener.onColumnAdded(column, f[0], f[1], f[2], t);
			} else {
				mListener.onColumnAdded(column, null, null, null, null);
			}
		}

	    return c;
	}
	
	/**
	 * Метод дает таблице вводные данные. Аналогичен одноименному методу в JFace TableViewer.
	 * Однако этот метод заточен под коллекции элментов, то есть передать можно только коллекцию
	 * элментов наследующих CommonDTO.
	 * 
	 * @param data
	 */
	public final void setInput(CommonDTOList<? extends CommonDTO> data) {
		mData = data;

		if((mTableViewer != null) && (!mTableViewer.getControl().isDisposed()))
			mTableViewer.setInput(mData);
	}

	/**
	 * Заменить провайдер контента. Провайдер уже создан на этапе конструирования таблицы,
	 * но в некоторых случаях его надо заменить на другой. Для этого используется этот метод,
	 * хотя в общем случае его использование нежелательно. 
	 * 
	 * @param provider
	 */
	public final void setContentProvider(IContentProvider provider) {
		mTableViewer.setContentProvider(provider);
	}
	
	/**
	 * Обновить содержимое таблицы.
	 */
	public final void refresh() {
		mTableViewer.refresh();
	}

	/**
	 * Установить высоту строки таблицы.
	 * 
	 * @param height
	 */
	public final void setRowHeight(Integer height) {
		mTableViewer.getTable().setData(RWT.CUSTOM_ITEM_HEIGHT, height);
	}

	/**
	 * Выбрать элемент по ID можно только, если он был сохранен в базе и
	 * у него реально есть этот ID. Если объект не был сохранен - то надо пользоваться
	 * selectByDTO().
	 * 
	 * @param id элемента в базе данных
	 * @throws DTOException
	 */
	public void selectById(String id) throws DTOException {
		for (Object item : mData.getDTOList().values()) {
			String rowId = (String)((CommonDTO)item).getDataField("id", "getId", "setId");
			if (id.equals(rowId)) {
				mTableViewer.setSelection(new StructuredSelection(item));
			}
		}
	}

	/**
	 * Выбрать элемент представленный заданным объектом DTO. При этом сравниваются не сами объекты,
	 * а их хеши.
	 * 
	 * @param dto
	 */
	public void selectByDTO(CommonDTO dto) {
		if (dto == null) {
			mTableViewer.setSelection(null);
			return;
		}
		
		for (Object item : mData.getDTOList().values()) {
			// Объекты могут быть не равны по содержанию, но у них должны быть одинаковые хеши
			if (item.hashCode() == dto.hashCode()) {
				mTableViewer.setSelection(new StructuredSelection(item));
			}
		}
	}
	
	/**
	 * Привязать листенер к таблице. Листенер обрабатывает все действия пользователя, которые
	 * он производит с таблицей внутри этого виджета (выделение, даблклик и т.п.).
	 * 
	 * @param listener
	 */
	public void addTableListener(CommonTableListener listener) {
		mListener = listener;
	}
	
	/**
	 * Открытый метод для инициализации таблицы,
	 * включает в себя создание колонок переопределенным методом
	 * createTableColumns().
	 */
	public void initTable() {
		createTableColumns();
	}
	
	/**
	 * Предоставление контента, создание провайдера. Переопределяется на реализациях таблицы.
	 * 
	 * @return
	 */
	abstract protected IContentProvider createContentProvider();
	
	/**
	 * Создание отображалок для таблицы. Переопределяется на реализациях таблицы.
	 * 
	 * @param columnIndex
	 * @return
	 */
	abstract protected ColumnLabelProvider createLabelProvider(int columnIndex);
	
	/**
	 * Метод выполняется перед передачей сообщения в listener привязанный к таблице.
	 * Выполнение происходит при клике на строке таблицы. При этом не важно, изменяется ли
	 * выделение или нет.
	 *  
	 * @param item элемент данных, как правило это CommonDTO
	 */	
	abstract protected void itemSelected(Object item);

	/**
	 * Получить источник данных, установленный для таблицы.
	 * 
	 * @return
	 */
	public Object getInput() {
		return mTableViewer.getInput();
	}

}

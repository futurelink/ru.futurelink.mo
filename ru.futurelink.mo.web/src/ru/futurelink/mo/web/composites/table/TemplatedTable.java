/**
 * 
 */
package ru.futurelink.mo.web.composites.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class TemplatedTable extends CommonComposite implements ICommonTable {

	private static final long serialVersionUID = 1L;

	private 	CommonDTOList<? extends CommonDTO>	mData;
	private		HashMap<CommonDTO, TableItem>			mIndex;
	private		CommonTableListener 					mListener;
	private		Table									mTable;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 */
	public TemplatedTable(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);
		
		mIndex = new HashMap<CommonDTO, TableItem>();

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);

		mTable = new Table(this, SWT.FULL_SELECTION);
		mTable.setHeaderVisible(false);
		mTable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		mTable.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		
		mTable.addSelectionListener(new SelectionListener() {			
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					if (mListener != null) {
						TableItem item = mTable.getItem(mTable.getSelectionIndex());
						for (Entry<CommonDTO, TableItem> e : mIndex.entrySet()) {
							if (e.getValue().equals(item)) {
								mListener.itemSelected(e.getKey());
								break;
							}
						}
					}
				} catch (DTOException e) {
					// TODO Ошибка обработки выбора элемента в таблице
				}				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		mTable.addMouseListener(new MouseListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {}
			
			@Override
			public void mouseDown(MouseEvent arg0) {}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				if (mListener != null) {
					TableItem item = mTable.getItem(mTable.getSelectionIndex());
					for (Entry<CommonDTO, TableItem> e : mIndex.entrySet()) {
						if (e.getValue().equals(item)) {
							mListener.itemDoubleClicked(e.getKey());
							break;
						}
					}
				}
			}
		});
		
		setRowHeight(48);
	
		Template template = new Template();

		TextCell nameCell = new TextCell( template );
		nameCell.setBindingIndex(0); // display text from column 1
		nameCell.setTop(8).setLeft(16).setWidth(180).setHeight(16);
		nameCell.setHorizontalAlignment( SWT.LEFT ); // left-align the text in this cell

		TextCell nameCell2 = new TextCell( template );
		nameCell2.setBindingIndex(1); // display text from column 1
		nameCell2.setTop( 24 ).setLeft( 16 ).setWidth( 180 ).setHeight( 16 );
		nameCell2.setHorizontalAlignment( SWT.LEFT ); // left-align the text in this cell
		//nameCell.setFont( font );
		
		mTable.setData( RWT.ROW_TEMPLATE, template );
	}

	@Override
	public void setInput(CommonDTOList<? extends CommonDTO> data) {
		mData = data;

		if (mData != null) {
			// Clear indexed items which are not in new data set
			for(Iterator<CommonDTO> iter = mIndex.keySet().iterator(); iter.hasNext();){
				CommonDTO currentDTO = iter.next();
				if (!mData.getDTOList().containsValue(currentDTO)) {
					int index = mTable.indexOf(mIndex.get(currentDTO));
					mTable.remove(index);	// Remove from table
					iter.remove();			// Remove from index
				}
			}

			// When the input is set we link TableItems to CommonDTO items
			// if they aren't linked.
			for (Integer index : mData.getOrderList().keySet()) {
				CommonDTO dto = mData.getDTOList().get(mData.getOrderList().get(index));
				TableItem item;
				if (mIndex.containsKey(dto)) {
					item = mIndex.get(dto);
				} else {
					item = new TableItem(mTable, SWT.NONE);					
					mIndex.put(dto, item);
				}

				displayItem(item, dto);
			}
		} else {
			mIndex.clear();
			mTable.clearAll();		
		}
	}

	private void displayItem(TableItem item, CommonDTO dto) {
		item.setText(0, dto.getDataFieldAsString("mTitle", "getTitle", "NONE"));
		item.setText(1, dto.getDataFieldAsString("mId", "getId", "-"));
	}
	
	@Override
	public Object getInput() {
		return mData;
	}

	@Override
	public void setContentProvider(IContentProvider provider) {

	}

	@Override
	public void setRowHeight(Integer height) {
		mTable.setData(RWT.CUSTOM_ITEM_HEIGHT, height);
	}

	@Override
	public void selectById(String id) throws DTOException {

	}

	@Override
	public void selectByDTO(CommonDTO dto) {

	}

	@Override
	public void addTableListener(CommonTableListener listener) {
		mListener = listener;
	}

	@Override
	public void initTable() {
		createTableColumns();
	}

	@Override
	public void createTableColumns() {
		new TableColumn(mTable, SWT.NONE);
		new TableColumn(mTable, SWT.NONE);
	}

}

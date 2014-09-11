/**
 * 
 */
package ru.futurelink.mo.web.composites.table;

import java.util.HashMap;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
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
	private 	IContentProvider 						mContentProvider;
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

		mTable = new Table(this, SWT.NONE);
		mTable.setHeaderVisible(false);
		mTable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		mTable.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		
		Template template = new Template();

		TextCell nameCell = new TextCell( template );
		nameCell.setBindingIndex(0); // display text from column 1
		nameCell.setLeft( 60 ).setWidth( 180 ).setTop( 30 ).setBottom( 8 );
		nameCell.setHorizontalAlignment( SWT.LEFT ); // left-align the text in this cell
		//nameCell.setFont( font );	
	}

	@Override
	public void setInput(CommonDTOList<? extends CommonDTO> data) {
		mData = data;

		// Clear indexed items which are not in new data set
		for (CommonDTO dto : mIndex.keySet()) {
			if (!data.getDTOList().containsValue(dto)) {
				int index = mTable.indexOf(mIndex.get(dto));
				mTable.remove(index);	// Remove from table
				mIndex.remove(dto);		// Remove from index
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
			
			//try {				
				item.setText(dto.getDataFieldAsString("mTitle", "getTitle", "NONE"));
			//} catch (DTOException ex) {
//				ex.printStackTrace();
			//}

		}
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

	}

	@Override
	public void selectById(String id) throws DTOException {

	}

	@Override
	public void selectByDTO(CommonDTO dto) {

	}

	@Override
	public void addTableListener(CommonTableListener listener) {

	}

	@Override
	public void initTable() {
		createTableColumns();
	}

	@Override
	public void createTableColumns() {
		
	}

}

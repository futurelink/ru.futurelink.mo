package ru.futurelink.mo.web.composites.toolbar;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.fields.FilterListener;

public class JournalToolbar extends CommonToolbar {

	private static final long serialVersionUID = 1L;

	private Button toolButton1;
	private Button toolButton2;
	private Button toolButton3;
	
	private FilterListener	mFilterListener;

	public JournalToolbar(ApplicationSession session, Composite parent, int style, FilterDTO filterDTO) {
		super(session, parent, style);
		
		boolean mobileMode = getSession().getMobileMode();
		
		toolButton1 = addButton("create");
		Image image1 = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/document_add.png"));
		toolButton1.setImage(image1);
		if (!mobileMode)
			toolButton1.setText(getLocaleString("create"));
		toolButton1.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(toolButton1);
			}
		});

		toolButton2 = addButton("edit");
		Image image2 = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/document_edit.png"));
		toolButton2.setImage(image2);
		if (!mobileMode)
			toolButton2.setText(getLocaleString("edit"));
		toolButton2.addSelectionListener(new SelectionAdapter() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(toolButton2);
			}
		});

		toolButton3 = addButton("delete");
		Image image3 = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/document_delete.png"));
		toolButton3.setImage(image3);
		if (!mobileMode)
			toolButton3.setText(getLocaleString("delete"));
		toolButton3.addSelectionListener(new SelectionAdapter() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(toolButton3);
			}
		});

	}

	public void addFilterListener(FilterListener listener) {
		mFilterListener = listener;
	}
	
	public FilterListener getFilterListener() {
		return mFilterListener;
	}
	
}

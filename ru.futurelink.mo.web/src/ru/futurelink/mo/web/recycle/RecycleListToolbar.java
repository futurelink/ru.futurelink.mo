package ru.futurelink.mo.web.recycle;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;

public class RecycleListToolbar extends CommonToolbar {

	private static final long serialVersionUID = 1L;

	private Button toolButton5;
	
	public RecycleListToolbar(ApplicationSession session, Composite parent,
			int style) {
		super(session, parent, style);

		toolButton5 = addButton("recycleRecover");		
		Image image5 = new Image(getDisplay(), CommonComposite.class.getResourceAsStream("/images/32/first_aid_box.png"));
		toolButton5.setImage(image5);
		toolButton5.setText(getLocaleString("recycleRecover"));
		toolButton5.addSelectionListener(new SelectionAdapter() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(toolButton5);
			}
		});
	}

}

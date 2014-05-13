package ru.futurelink.mo.web.composites.toolbar;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;

public class ItemToolbar extends CommonToolbar {

	private static final long serialVersionUID = 1L;

	Button saveButton;
	Button closeButton;
	
	public ItemToolbar(ApplicationSession session, Composite parent, int style) {
		super(session, parent, style);

		saveButton = addButton("save");
		Image image1 = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/check2.png"));
		saveButton.setImage(image1);
		saveButton.setText(getLocaleString("save"));
		saveButton.addSelectionListener(new SelectionAdapter() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(saveButton);
			}
		});
		
		closeButton = addButton("close");
		Image image2 = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/delete2.png"));
		closeButton.setImage(image2);
		closeButton.setText(getLocaleString("close"));
		closeButton.addSelectionListener(new SelectionAdapter() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeListener(closeButton);
			}
		});

	}

	public Button getSaveButton() {
		return saveButton;
	}

	public Button getCloseButton() {
		return closeButton;
	}
	
}

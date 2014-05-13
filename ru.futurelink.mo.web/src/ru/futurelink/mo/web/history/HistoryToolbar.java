package ru.futurelink.mo.web.history;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;

public class HistoryToolbar extends CommonToolbar {

	private static final long serialVersionUID = 1L;

	public HistoryToolbar(ApplicationSession session, Composite parent,
			int style) {
		super(session, parent, style);
	}

}

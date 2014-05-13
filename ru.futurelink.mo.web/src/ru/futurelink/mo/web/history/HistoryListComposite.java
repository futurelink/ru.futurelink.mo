package ru.futurelink.mo.web.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @since 1.2
 */
public class HistoryListComposite extends SimpleListComposite {

	private static final long serialVersionUID = 1L;

	public HistoryListComposite(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style | SWT.BORDER, params);
	}

	@Override
	protected CommonToolbar createToolbar() {
		return null;
	}
	
}

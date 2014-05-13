package ru.futurelink.mo.web.recycle;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @since 1.2
 */
public class RecycleListComposite extends SimpleListComposite {

	private static final long serialVersionUID = 1L;

	public RecycleListComposite(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style | SWT.BORDER, params);
	}
	
	@Override
	protected CommonToolbar createToolbar() {
		return new RecycleListToolbar(getSession(), this, SWT.NONE);
	}
	
	protected void setTableContentProvider(IContentProvider provider) {
		mTable.setContentProvider(provider);
	}
	
}

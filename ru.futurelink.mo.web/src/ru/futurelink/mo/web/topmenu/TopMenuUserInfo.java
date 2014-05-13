/**
 * 
 */
package ru.futurelink.mo.web.topmenu;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class TopMenuUserInfo extends CommonComposite {

	private static final long serialVersionUID = 1L;

	/**
	 * @param session
	 * @param container
	 * @param style
	 * @param params
	 */
	public TopMenuUserInfo(ApplicationSession session, Composite container,
			int style, CompositeParams params) {
		super(session, container, style, params);

		setLayout(new GridLayout());
		
		setData(RWT.CUSTOM_VARIANT, "topMenuUserInfo");
	}
}

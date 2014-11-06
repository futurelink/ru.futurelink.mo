package ru.futurelink.mo.demo.app;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.app.ApplicationWindow;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.GradientedSeparator;


public class DemoApplicationWindow extends ApplicationWindow {

	private static final long serialVersionUID = 1L;

	private Composite					mMainComposite;
	private CommonComposite				mContentWindow;
	private CommonComposite				mTopMenuContainer;
	
	public DemoApplicationWindow(ApplicationSession session, Composite parent) {
		super(session, parent);
	}

	@Override
	protected void createMainWindow() {
		try {
			GridData gd = null;

		    // Создаем область для всего окна
			mMainComposite = new Composite(this, SWT.NONE);
			mMainComposite.setData(RWT.CUSTOM_VARIANT, "commonComposite");
			GridLayout mainLayout = new GridLayout();
			mainLayout.numColumns = 1;
			mainLayout.verticalSpacing = 0;
			mainLayout.horizontalSpacing = 0;
			mainLayout.marginWidth = 0;
			mainLayout.marginHeight = 0;
			mMainComposite.setLayout(mainLayout);

			// Добавляем контейнер меню, только в полной версии,
			// для мобильной оно не нужно!
			if (!getSession().getMobileMode()) {
				mTopMenuContainer = new CommonComposite(getSession(), mMainComposite, SWT.NONE, null);
				gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				gd.verticalAlignment = GridData.BEGINNING;
				gd.grabExcessHorizontalSpace = true;
				gd.heightHint = 128;
				mTopMenuContainer.setLayoutData(gd);
				mTopMenuContainer.setLayout(new FillLayout());
			}

		    GradientedSeparator sep = new GradientedSeparator(mMainComposite, SWT.NONE);
			GridData sepgd = new GridData();
			sepgd.horizontalAlignment = GridData.FILL;
			sepgd.grabExcessHorizontalSpace = true;
			sepgd.verticalAlignment = GridData.BEGINNING;
			sepgd.grabExcessVerticalSpace = false;
			sepgd.heightHint = 8;
		    sep.setLayoutData(sepgd);

			// Добавляем рабочую область
			Composite mWorkComposite = new Composite(mMainComposite, SWT.NONE);
			mWorkComposite.setData(RWT.CUSTOM_VARIANT, "commonComposite");
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessVerticalSpace = true;
			mWorkComposite.setLayoutData(gd);

			GridLayout workLayout = new GridLayout();
			workLayout.numColumns = 1;
		    if (getSession().getMobileMode()) {
		    	workLayout.verticalSpacing = 0;
		    	workLayout.horizontalSpacing = 0;
		    	workLayout.marginWidth = 0;
		    	workLayout.marginHeight = 0;		    	
		    } else {
		    	workLayout.verticalSpacing = 24;
		    	workLayout.horizontalSpacing = 24;
		    	workLayout.marginWidth = 24;
		    	workLayout.marginHeight = 24;
		    }
			mWorkComposite.setLayout(workLayout);

			// Создаем контейнер для рабочей области, куда будут запускаться юзкейсы.
			// Этот контейнер будет для запуска субконтроллера MainCompositeController.
			mContentWindow = new CommonComposite(getSession(), mWorkComposite, SWT.NONE, null);
			mContentWindow.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
			mContentWindow.setLayout(new FillLayout());

			layout();
		} catch (Exception ex) {
			getSession().logger().error("Error creating main window!", ex);
		}
	}

	public CommonComposite getMainComposite() {
		return mContentWindow;
	}
	
	@SuppressWarnings("unused")
	private void clearContentWindow() {
		Control[] controls = mContentWindow.getChildren();
		for (int i = 0; i < controls.length; i++) {
			if(controls[i] != null) {
				controls[i].dispose();
			}
		}
	}

	public Composite getTopMenuContainer() {
		return mTopMenuContainer;
	}
	
}

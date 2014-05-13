package ru.futurelink.mo.web.composites.toolbar;

import java.util.HashMap;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.GradientedSeparator;
import ru.futurelink.mo.web.controller.CompositeParams;

public class CommonToolbar extends CommonComposite {

	private static final long serialVersionUID = 1L;

	private ToolbarListener 	mListener = null;
	private GridLayout 			mLayout;
	private CommonComposite		mContainer;

	private HashMap<String, Control> mControls;
	
	public CommonToolbar(ApplicationSession session, Composite parent, int style) {
		super(session, parent, style, null);

		mControls = new HashMap<String, Control>();

		GridLayout l = new GridLayout();
		l.numColumns = 1;
		l.marginTop = 0;
		l.marginBottom = 0;
		l.marginLeft = 0;
		l.marginRight = 0;
		l.marginWidth = 0;
		l.marginHeight = 0;
		l.verticalSpacing = 0;
		setLayout(l);

		mContainer = new CommonComposite(session, this, SWT.NONE, new CompositeParams());
		mContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		GradientedSeparator sep = new GradientedSeparator(this, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.heightHint = 4;
		sep.setLayoutData(gd);

		mLayout = new GridLayout();
		mLayout.numColumns = 0;
		mLayout.marginTop = 0;
		mLayout.marginWidth = 0;
		mLayout.marginHeight = 0;
		mLayout.horizontalSpacing = 1;
		mLayout.verticalSpacing = 0;
		mContainer.setLayout(mLayout);
		mContainer.setData(RWT.CUSTOM_VARIANT, "toolBar");		
	}
	
	public ToolbarListener getToolbarListener() {
		return mListener;
	}

	public void addToolBarListener(ToolbarListener listener) {
		mListener = listener;
	}
	
	protected void executeListener(Button button) {
		if (mListener != null) {
			mListener.toolBarButtonPressed(button);
		}
	}
	
	public CommonComposite getContainer() {
		return mContainer;
	}
	
	public void addColumn() {
		mLayout.numColumns++;
	}
	
	public Button addButton(String buttonName) {
		mLayout.numColumns++;
		Button button = new Button(mContainer, SWT.PUSH);
		button.setData(RWT.CUSTOM_VARIANT, "toolButton");
		button.setData(buttonName);

		mControls.put(buttonName, button);
		
		return button;
	}
	
	/**
	 * Разрешить или запретить контрол на панели инструментов.
	 * Метод ищет среди добавленных на панель контролов по имени нужный,
	 * и проставляет ему флаг запрещения. Если контрол по имени не найден,
	 * то ничего не происходит.
	 * 
	 * Пока что работает только с кнопками, так как только они добавляются
	 * в стэк контролов панели.
	 * 
	 * @param buttonData
	 * @param enabled
	 */
	public void setControlEnabled(String buttonData, boolean enabled) {
		if (mControls.containsKey(buttonData) && (!mControls.get(buttonData).isDisposed()))
			mControls.get(buttonData).setEnabled(enabled);
	}
	
	/**
	 * Разрешить или запретить все контролы на панели инструментов.
	 * 
	 * @param enabled
	 */
	public void setControlsEnabled(boolean enabled) {
		for (String c : mControls.keySet()) {
			mControls.get(c).setEnabled(enabled);
		}
	}
	
	public void addSpacer() {
		mLayout.numColumns++;
		Label separator = new Label(mContainer, SWT.NONE);
		separator.setBackground(new Color(getDisplay(), 100, 100, 100));
		separator.setSize(16, 8);		
	}
}

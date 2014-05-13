package ru.futurelink.mo.web.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * <p>Класс композита имеющий доступ к модели данных через CommonDTO. Этот вид композитов включает в
 * себя Layout, а также содержимое представленное рабочей областью и тулбаром. При этом положение
 * рабочей области и турбара определяется в методе createContents().</p>
 * 
 * <p>Создание рабочей области происходит в createWorkspace, а создание тулбара в createToolbar.
 * Также такой копозит содержит в себе caption.</p>
 * 
 * <p><i>Этот класс необходимо переопределить и реализвать абстрактные методы.</i></p>
 * 
 * @author pavlov
 *
 */
public abstract class CommonDataComposite extends CommonComposite
{
	private static final long serialVersionUID = 1L;

	private		Label 				mCaptionLabel;
	protected 	CommonToolbar 		mToolbar;
	protected 	CommonComposite 	mWorkspace;
	
	public CommonDataComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);	
	}

	protected CommonDataComposite(Composite parent) { super(parent); }

	@Override
	public void init() throws InitException {
		super.init();

		// В этом методе должен быть создан интерфейс композита.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 8;
		setLayout(layout);

		// Создаем содержимое окна. Если не получилось -
		// выбрасываем исключение и выходим.
		try {			
			createContents();
		} catch(CreationException ex) {
			layout = null;
			setLayout(null);
			throw new InitException(ex.getMessage());
		}

		if (mToolbar != null) {
			mToolbar.setLayoutData(
					new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL)
				);
			mToolbar.pack();
		}
		if (mWorkspace != null) {
			mWorkspace.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
					GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | 
					GridData.GRAB_VERTICAL));
			mWorkspace.pack();
		}	
		
		layout();
	}
	
	/**
	 * Задает текст заголовка композита.
	 * 
	 * @param caption
	 */
	final public void setCaption(String caption) {
		mCaptionLabel.setText(caption);
		if (!caption.equals("")) {
			setCaptionLabelVisible(true);
		} else {
			setCaptionLabelVisible(false);
		}
	}
	
	final private void setCaptionLabelVisible(boolean visible) {
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		if (!visible) { gd.exclude = true; }
		
		mCaptionLabel.getParent().setVisible(visible);
		mCaptionLabel.getParent().pack();
		mCaptionLabel.getParent().setLayoutData(gd);
		layout();
	}
	
	/**
	 * Метод создание заголовка композита. Нужно переопределить, чтобы создать
	 * собственный заголовок или изменить стиль заголовка.
	 */
	final protected void createCaptionLabel() {
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		Composite c = new Composite(this, SWT.NONE);
		c.setForeground(new Color(c.getDisplay(), 255, 255, 255));
		c.setBackground(new Color(c.getDisplay(), 50, 100, 150));
		c.setLayoutData(gd);

		c.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		GridLayout cg = new GridLayout();
		cg.marginWidth = 16;
		cg.marginHeight = 8;
		cg.numColumns = 1;
		c.setLayout(cg);
		
		mCaptionLabel = new Label(c, SWT.NONE);
		FontData[] fD = mCaptionLabel.getFont().getFontData(); fD[0].setHeight(20);
		mCaptionLabel.setFont( new Font(mCaptionLabel.getDisplay(), fD[0]));
		mCaptionLabel.setForeground(new Color(c.getDisplay(), 255, 255, 255));
		mCaptionLabel.setLayoutData(gd);

		setCaptionLabelVisible(false);
	}

	/**
	 * Метод создает содержимое композита. По-умолчанию тулбар сверху рабочей области окна.
	 * Метод переопределн в CommonDataItemComposite, чтобы тулбар был внизу и содержах кнопки,
	 * характерные для элемента данных или диалогового окна.
	 */
	protected void createContents() throws CreationException {
		//createCaptionLabel();
		mToolbar = createToolbar();
		mWorkspace = createWorkspace();

		// Если не получилось создать рабочее пространство
		// отменяем создание всего.
		/*if (mWorkspace == null) {
			if ((mToolbar != null) && (!mToolbar.isDisposed()))
				mToolbar.dispose();
				mToolbar = null;
				dispose();
		}*/
	}
	
	/**
	 * Возвращает объект тулбара композита.
	 * 
	 * @return
	 */
	public CommonToolbar getToolbar() {
		return mToolbar;
	}

	public CommonComposite getWorkspace() {
		return mWorkspace;
	}
	
	/**
	 * Запретить все кнопки на панели инструментов.
	 */
	public void disableToolbar() {
		if (mToolbar != null)
			mToolbar.setControlsEnabled(false);		
	}

	public void setToolEnabled(String toolName, boolean enabled) {
		if (mToolbar != null)
			mToolbar.setControlEnabled(toolName, enabled);
	}
	
	/**
	 * Создает основную область окна, переопределить в
	 * дочерних классах.
	 * @throws CreationException 
	 */
	protected abstract CommonComposite createWorkspace() throws CreationException;
	
	/**
	 * Создает тулбар окна, переопределить в дочерних классах. 
	 */
	protected abstract CommonToolbar createToolbar();
}

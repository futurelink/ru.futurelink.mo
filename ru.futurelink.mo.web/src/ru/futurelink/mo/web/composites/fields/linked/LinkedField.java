/**
 * 
 */
package ru.futurelink.mo.web.composites.fields.linked;

import java.util.HashMap;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.composites.CommonPopup;
import ru.futurelink.mo.web.composites.fields.TextField;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Текстовое поле с автодополнением и выпадающим списком работает так:
 * - вводимое значение дополняется по мере ввода слов. Как только введенное значение
 *   совпадает со словом или набором слов, сразу выпадает список выбора возхможных значений
 *   и появляется дополнение - дополнение выделено.
 *   
 * - слово, это сколько угодно букв и пробел, или более трех букв без пробела.
 * 
 * - для подбора используется специальный пословный индекс, по которому идет поиск, создаваемые
 *   значения сразу разбиваются на слова и добавляются в индексную таблицу вида "слово", "позиция", "значение".
 *   где слово - текстовое значение слова, позиция - место слова в предложении, значение - ИД предложения.
 *   Таким образом поиск предложений подходящих по условию сводится к поиску всех слов, которые мы ввели
 *   на нужных позициях. JOIN на две таблицы - индексная и предложения, и получаем нужные данные. 
 * 
 * @author pavlov
 *
 */
public class LinkedField extends TextField {
	private FocusListener	mFocusListener;
	private CommonPopup		mPopup;
	private boolean		justShown;
	private HashMap<String, Object> mSelectionParams;
	private ModifyListener mModifyListener;
	
	// Костыль, для того, чтобы когда показываем попап и теряется
	// фокус - это не считалось окончанием редактирования.
	private boolean		mPopupShowingState;
	
	// Контроллер автодополнения и связанного списка подбора
	private LinkedListController mLinkedListController;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 * @param dataComposite
	 */
	public LinkedField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);

		mPopup = new CommonPopup(mParent.getSession(), mParent.getShell(), SWT.BORDER);
		mSelectionParams = new HashMap<String, Object>();

		// Убираем старый обработчик, который создал конструктор
		((Text)mControl).removeModifyListener(mModifyListener);

		// Задаем новый обработчик
		mModifyListener = new ModifyListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void modifyText(ModifyEvent arg0) {
				try {
					
					// Если текст изменился на подсказку - то считаем что текст пустой
					if (((Text)mControl).getText().equals(getHint())) {
						mRealText = new String();
					} else {
						mRealText = String.copyValueOf(((Text)mControl).getText().toCharArray());
					}

					// Вызываем этот метод только для DTO предназначенных для редакитрования,
					// если текст изменен на hint надо его занулить.
					if ((getDTO() != null) && EditorDTO.class.isAssignableFrom(getDTO().getClass())) {
						if ((getText() == null) || getText().equals(getHint())) {
							getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, "");
						} else {
							getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, getText());
						}
					}

					if (getControllerListener() != null)
						((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());

					// Показываем всплывающее окно с автодополнением
					setSelectionParam("text", getText());
					try {
						handlePopup();
					} catch (InitException ex) {
						
					}

					// Обрабатываем автодополнение
					handleAutoComplete();
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления текстового поля!", ex);
				}
			}
		};

		((Text)mControl).addModifyListener(mModifyListener);

		((Text)mControl).addKeyListener(new KeyListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void keyReleased(KeyEvent event) {
			}

			@Override
			public void keyPressed(KeyEvent event) {			
				// Отлавливаем нажатие Enter и ESC
				switch (event.keyCode) {
				case SWT.CR:
					enterValue();
					event.doit = false;
					break;
				case SWT.ESC:
					// Скрываем попап при первом нажатии - если он показан
					if (justShown) {
						justShown = false;				
						mPopup.close();
						if (mLinkedListController != null)
							mLinkedListController.uninit(); // Если контроллер был инициализирован, возвращаем состояние
					} else
						// А если не показан или нажали второй раз - отменяем изменения данных
						dateChangeCancelled();
					break;
				case 16777218:
					if (mLinkedListController != null) {
						((LinkedListControllerListener)mLinkedListController.getControllerListener()).selectNext();
						event.doit = false;
					}
					break;
				case 16777217:
					if (mLinkedListController != null) {
						((LinkedListControllerListener)mLinkedListController.getControllerListener()).selectPrev();
						event.doit = false;
					}
					break;
				}				
			}
		});

		mControl.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent arg0) {			
				// Если фокус потерян по причине того, что показан попап - не учитываем это.
				if (!mPopupShowingState) {

					// Разрешаем ESCAPE (специфично для RWT)
					Display.getCurrent().setData( RWT.CANCEL_KEYS, new String[] {} );
					
					// Если в данных пусто - покажем подсказку
					if (((Text)mControl).getText().equals("")) {
						((Text)mControl).setText(getHint());
					}

					dataChangeFinished();

					if (mFocusListener != null)
						mFocusListener.focusLost(arg0);
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// При всплывании фокус теряется, не считаем это изменением данных
				if (!mPopupShowingState) { 
					mInitialText = String.copyValueOf(getText().toCharArray());

					// Запрещаем ESCAPE (специфично для RWT)
					Display.getCurrent().setData( RWT.CANCEL_KEYS, new String[] { "ESC", "ESCAPE" } );				
				} else 
					mPopupShowingState = false;
				
				// Сбросим подсказку, если получили фокус, и текст равер подсказке
				if (((Text)mControl).getText().equals(getHint())) {
					((Text)mControl).setText("");
				}
				if (mFocusListener != null)
					mFocusListener.focusGained(arg0);
			}
		});
	}

	/**
	 * Обработка всплывания окна с автодополнением и  предложением выбора из
	 * списка... или еще какого-то там окна с чем-то там.
	 * 
	 * В списке должны выводиться данные, которые также и содержат введенный
	 * кусок текста, а не только с него начинаются.
	 * @throws InitException 
	 */
	private void handlePopup() throws InitException {
		if (((Text)mControl).getText().length() > 3) {
			if (!((Text)mControl).getText().equals(getHint()) && mControl.isFocusControl()) {
				if (!justShown) {
					if (mLinkedListController != null) {

						mPopupShowingState = true;
						
						Point coords = ((Text)mControl).toDisplay(0, 2);
						Rectangle bounds = ((Text)mControl).getBounds();
						mPopup.open();
						
						// Внедряем во всплывающее окно композит контроллера
						mLinkedListController.setContainer(mPopup.getShell());
						mLinkedListController.init(); // Инициализация, создает композит и все такое
						if (mLinkedListController.getComposite() != null) {
							mLinkedListController.handleDataQuery(8, mSelectionParams);
							mPopup.attachComposite(mLinkedListController.getComposite());
						} else {
							mParent.getSession().logger().error("No composite for LinkedList created!");							
						}
						
						mPopup.moveTo(new Point(coords.x-1, coords.y+bounds.height-1));
						mPopup.setSize(bounds.width-2, 250);
					}
					justShown = true;
				}
			}
		} else {
			if (justShown) {
				justShown = false;
				mPopup.close();
				if (mLinkedListController != null)
					mLinkedListController.uninit(); // Если контроллер был инициализирован, возвращаем состояние
			}
		}
	}

	public void setSelectionParam(String paramName, Object paramValue) {
		mSelectionParams.put(paramName, paramValue);
	}
	
	private void handleAutoComplete() {
		// Получаем наиболее перспективные данные,
		// дополняем ими наше поле ввода.
		// Но выборка должна происходить только если вводится слово целиком,
		// или несколько слов, но не буква или набор букв.
		String completedText = "Банк Приморье";
		String text = ((Text)mControl).getText(); 
		if (text.equals("Банк")) {
			((Text)mControl).setText(completedText);
			((Text)mControl).setSelection(text.length(), completedText.length());
		}
	}

	/**
	 * Обработка завершения ввода данных.
	 */
	protected void dataChangeFinished() {			
		// Скрываем попап, если контрол потерял фокус
		if (justShown) {
			justShown = false;				
			mPopup.close();
			if (mLinkedListController != null)
				mLinkedListController.uninit(); // Если контроллер был инициализирован, возвращаем состояние
		}

		// Дальше не идем, если данные реально не изменились
		if (!getIsDataChanged()) return;
		
		// Если данные поменялись, сохраняем новое значение
		mInitialText = String.copyValueOf(mRealText.toCharArray());
		
		try {
			if (getControllerListener() != null)
				((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
		} catch (DTOException e) {
			getControllerListener().sendError("Ошибка обновления текстового поля!", e);
		}
	}


	/**
	 * 
	 */
	protected void dateChangeCancelled() {
		
	}

	/**
	 * Ввод значения в поле. Вызывается из обработчика клавиатурного ввода
	 * в самом поле, а также может вызываться из обработчика двойного клика
	 * поля связанного списка.
	 */
	public final void enterValue() {
		// Если выбран элемент в выпадающейм списке, надо вставить его
		if (mLinkedListController != null) {
			String data = mLinkedListController.getSelected();
			if ((data != null) && !data.equals(""))
				try {
					setText(data);
				} catch (DTOException ex) {
					mLinkedListController.handleError("Ошибка обновления данных!", ex);
				}
		}
		
		// Для энтера такая же отработка, как для потери фокуса
		dataChangeFinished();
	}

	/**
	 * Привязать к полю контроллер списка автодополнения.
	 * 
	 * @param controller
	 */
	public void addAutoCompleteController(LinkedListController controller) {
		mLinkedListController = controller;
	}

}

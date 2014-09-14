/**
 * 
 */
package ru.futurelink.mo.web.composites.fields.linked;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.TypedQuery;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Базовый контроллер автодополнения.
 * 
 * @author pavlov
 *
 */
public class LinkedListController extends CompositeController {

	private ArrayList<String> mItems; // Ссылка на список будет существовать, пока есть контроллер
	
	/**
	 * @param parentController
	 * @param dataClass
	 * @param container
	 * @param compositeParams
	 */
	public LinkedListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass,
			CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
	}	

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		LinkedListComposite c = new LinkedListComposite(getSession(), getContainer(), SWT.BORDER, params);
		c.addControllerListener(createControllerListener());

		return c;
	}

	@Override
	protected void doBeforeCreateComposite() {
		// Так как контроллер постоянно освобождается при скрытии
		// поп-апа, пересоздаем список до создания композита, а не в
		// конструкторе.
		mItems = new ArrayList<String>();
	}

	@Override
	protected void doAfterCreateComposite() {
		((LinkedListComposite)getComposite()).setInput(mItems);
	}

	@Override
	protected void doBeforeInit() {
	}

	@Override
	protected void doAfterInit() {
	}

	@Override
	public CommonControllerListener createControllerListener() {
		return new LinkedListControllerListener() {			
			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}
			
			@Override
			public void autoCompleteSelected(String selectionText) {
				handleAutoCompleteSelected(selectionText);
				
				// Передаем обратно на контроллер выше данные
				if (getControllerListener() != null) {
					((LinkedListControllerListener)getControllerListener()).autoCompleteSelected(selectionText);
				}
			}

			@Override
			public void selectNext() {}

			@Override
			public void selectPrev() {}

			@Override
			public void autoCompleteEntered() {
				handleAutoCompleteEntered();
				
				// Передаем обратно на контроллер выше данные
				if (getControllerListener() != null) {
					((LinkedListControllerListener)getControllerListener()).autoCompleteEntered();
				}				
			}
		};
	}

	/**
	 * Получить данные для списка автодополнения.
	 */
	public void handleDataQuery(int itemsCount, HashMap<String, Object> params) {
		String initialText = (String)params.get("text");
		Double lon = params.get("lon") != null ? (Double)params.get("lon") : 0.0;
		Double lat = params.get("lat") != null ? (Double)params.get("lat") : 0.0;
		Double square = 20.0;

		mItems.clear();
		// Делаем тупую выборку, пока что сделано так, потом, если будет
		// сильно тормозить, надо будет сделать хитрую систему индексирования.
		
		// Задача - выбрать все места, точки которых находятся ближе всего к заданной,
		// отсортированными по расстоянию до заданной точки. Если точек нет - они в конце выборки.
		TypedQuery<String> q = getSession().persistent().getPersistent().getEm().createQuery(
				"select DISTINCT p.mPlace.mTitle "
				+ "from CommonGeoPoint p where p.mPlace.mTitle like :text and "
				+ "p.mAbsoluteLongtitude > :absLon1 and p.mAbsoluteLongtitude < :absLon2 and "
				+ "p.mAbsoluteLatitude > :absLat1 and p.mAbsoluteLatitude < :absLat2 and "
				+ "p.mPlace.mType <> :private", 
				String.class);
		q.setParameter("text", "%"+initialText+"%");
		q.setParameter("private", "placePrivate");	// Места с типом "частное" не выводим
		q.setParameter("absLat1", lat * 111.111 - square);
		q.setParameter("absLat2", lat * 111.111 + square);
		q.setParameter("absLon1", lon * 111.3 * Math.cos(Math.PI * lat / 180) - square);
		q.setParameter("absLon2", lon * 111.3 * Math.cos(Math.PI * lat / 180) + square);
		if (q.getResultList().size() > 0) {
			for(int i = 0; (i < q.getResultList().size()) && (i < itemsCount); i++) {
				mItems.add(q.getResultList().get(i));
			}
		}
		((LinkedListComposite)getComposite()).refresh();
	}

	/**
	 * Метод получения автодополненной строки на основе начальной строки.
	 * 
	 * @param initialString
	 * @return
	 */
	public String getAutoComplete(String initialString) {
		return "";
	}
	
	/**
	 * Обработка пользовательских данных, которые невозможно дополнить,
	 * которые не соответствуют ни одному из дополнений. То есть пользователь
	 * ввел что-то свое, а не то, что ему предлагалось.
	 * 
	 * @param data
	 */
	public void handleUncompletedData(String data) {
		
	}
	
	/**
	 * Обработка пользовательских данных, которые удалось дополнить, то есть
	 * пользователь ввел вариант предложенный системой.
	 * 
	 * @param data
	 */
	public void handleAutoCompleteSelected(String selectionText) {
		logger().debug("Выбран элемент автодополнения: {}", selectionText);
	}

	public void handleAutoCompleteEntered() {
		
	}
	
	/**
	 * @param container
	 */
	public void setContainer(Composite container) {
		mContainer = container;
	}

	@Override
	public void uninit() {
		mItems.clear();	// Удаляем список элементов
		mItems = null;
		
		super.uninit();
	}

	/**
	 * Выбрать следующий элемент
	 */
	public void selectNext() {
		if (getComposite() != null)
			((LinkedListComposite)getComposite()).selectNext();
	}

	/**
	 * Выбрать предыдущий элемент 
	 */
	public void selectPrev() {
		if (getComposite() != null)
			((LinkedListComposite)getComposite()).selectPrev();
	}

	/**
	 * Получить выбранный элемент
	 * @return
	 */
	public String getSelected() {
		if (getComposite() != null)
			return ((LinkedListComposite)getComposite()).getSelected();
		return null;
	}

}

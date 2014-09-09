/**
 * 
 */
package ru.futurelink.mo.web.composites.fields.datapicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.HistoryObject;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.access.AllowAllChecker;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Этот контроллер используется для формированию окна выбора состоящего из
 * одной таблицы, сформированной на основе фильтрованной запросом выборки элементов
 * одного типа.
 * 
 * @author pavlov
 *
 */
public class SimpleDataPickerController extends CommonDataPickerController {

	private Class<?>				mTableClass;

	private Map<String, ArrayList<Object>> mQueryConditions;
	private String					mOrderBy;
	private boolean				mPublic;

	private EditorDTOList<EditorDTO> mList;

	/**
	 * @param parentController
	 * @param dataClass
	 * @param container
	 * @param compositeParams
	 */
	@SuppressWarnings("unchecked")
	public SimpleDataPickerController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
		
		if (compositeParams.get("queryConditions") != null) {
			mQueryConditions = (Map<String, ArrayList<Object>>) compositeParams.get("queryConditions"); 
		}
		
		if (compositeParams.get("orderBy") != null) {
			mOrderBy = (String)compositeParams.get("orderBy");
		}
		
		if (compositeParams.get("public") != null) {
			mPublic = (Boolean)compositeParams.get("public");
		}
		
		// For public data pickers set allow all access checker to DTO
		IDTOAccessChecker accessChecker = null;
		if (mPublic) {
			accessChecker = new AllowAllChecker();
		} else {
			accessChecker = new AllowOwnChecker(getSession().getUser());
		}
		
		mList = new EditorDTOList<EditorDTO>(getSession().persistent(), accessChecker, EditorDTO.class);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleDataQuery() throws DTOException {		
		// Выполняем именованый запрос всех затратных документов нашего пользователя
		logger().debug("Запрос данных списка для просмотра...");
		TypedQuery<?> q2;
		String queryString = "";
		if (HistoryObject.class.isAssignableFrom(mDataClass)) {
			queryString = 
				"select d from "+mDataClass.getName()+" d where d.mDeleteFlag = 0 and d.mOutdated = 0";
		} else {
			queryString = 
				"select d from "+mDataClass.getName()+" d where d.mDeleteFlag = 0";
		}
		
		// If data picker is not public use only creator filtered records to select
		if (!mPublic) {
			 queryString += " and d.mCreator = :creator";
		}
		
		// Перелопатим допусловия первый раз...
		HashMap<String, Object> additionalValues = new HashMap<>();
		if (mQueryConditions != null) {
			int k = 0;
			ArrayList<String> additionalConditions = new ArrayList<String>();
			for (String fieldName : mQueryConditions.keySet()) {
				k++;
				String cond = ""; 
				for (int n = 0; n < mQueryConditions.get(fieldName).size(); n++) {
					cond = cond + "d." + fieldName + " = :fieldData" + k + n;
					additionalValues.put("fieldData" + k + n, mQueryConditions.get(fieldName).get(n));
					if (n < mQueryConditions.get(fieldName).size()-1)
						cond = cond + " or ";
				}
				cond = "("+ cond +")";
				additionalConditions.add(cond);
			}
			if (additionalConditions.size() > 0)
				queryString = queryString + " and " + CommonDataPickerController.join(additionalConditions, " and ");
		}
		logger().debug("Допонительные условия для выбора из списка: {}", queryString);

		// Выставляем сортировку, после того, как сформировали запрос
		if ((mOrderBy != null) && (!mOrderBy.isEmpty())) {
			queryString += " order by d."+mOrderBy;
		}

		q2 = mSession.persistent().getPersistent().getEm().createQuery(queryString, mDataClass);
		if (!mPublic) q2.setParameter("creator", getSession().getDatabaseUser());
		if (additionalValues.size() > 0) {
			for (String key : additionalValues.keySet()) {
				q2.setParameter(key, additionalValues.get(key));
			}
		}
		
		if (q2.getResultList().size() > 0) {
			logger().debug("Количество элементов: {}", q2.getResultList().size());
		}

		mList.clear();
		mList.addObjectList((List<? extends CommonObject>) q2.getResultList());
		
		handleDataQueryExecuted();
	}
	
	@Override
	protected void doAfterInit() {
		try {
			setDTO(mList);
		} catch (DTOException ex) {
			ex.printStackTrace();
		}
		
		super.doAfterInit();
	}
	
	@Override
	protected CommonComposite createComposite(CompositeParams compositeParams) {
		mComposite = new SimpleDataPickerComposite(getSession(), getContainer(), SWT.BORDER, mTableClass, params());

		return mComposite;
	}

	@Override
	protected void doAfterCreateComposite() {
		super.doAfterCreateComposite();

		((SimpleDataPickerComposite)getComposite()).createTableColumns();
	}

	@Override
	protected void doBeforeCreateComposite() {
		
	}

	@Override
	public void handleRecover() throws DTOException {
		
	};
}

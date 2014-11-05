/*******************************************************************************
 * Copyright (c) 2013-2014 Pavlov Denis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pavlov Denis - initial API and implementation
 ******************************************************************************/

package ru.futurelink.mo.web.history;

import java.util.List;

import javax.persistence.TypedQuery;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.HistoryObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.ViewerDTO;
import ru.futurelink.mo.orm.dto.ViewerDTOList;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.SimpleListController;

public class HistoryListController extends SimpleListController {

	private Long	mCode;
	
	private ViewerDTOList<ViewerDTO> mList;

	public HistoryListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	public HistoryListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleDataQuery() throws DTOException {
		mCode = (Long) params().get("code");
		
		mList = new ViewerDTOList<ViewerDTO>(new AllowOwnChecker(getSession().getUser()), ViewerDTO.class);

		logger().debug("Запрос данных истории");
		
		String query;
		TypedQuery<?> q2 = null;
		if (HistoryObject.class.isAssignableFrom(getDataClass())) {
			query =	"SELECT new ru.futurelink.mo.orm.HistoryResult(d.mId, d.mModifyDate, d.mWorkLog.mDescription, d.mId) FROM " + 
				getDataClass().getSimpleName()+" d " +
				"where d.mCreator = :creator and d.mCode.mId = :code " +
				"order by d.mModifyDate desc";

			q2 = mSession.persistent().getPersistentManager().getEm().createQuery(query, getDataClass());
			q2.setParameter("creator", getSession().getDatabaseUser());
			q2.setParameter("code", mCode);
		} else {
			// Найдем ID того объекта, для которого будем выбирать историю из лога
			TypedQuery<String> q1 = mSession.persistent().getPersistentManager().getEm().createQuery(
					"SELECT d.mId FROM " +
					getDataClass().getSimpleName()+" d "+
					"WHERE d.mCreator = :creator AND d.mCode.mId = :code", String.class);
			q1.setParameter("creator", getSession().getDatabaseUser());
			q1.setParameter("code", mCode);			
			if (q1.getResultList().size() > 0) {
				String objectId = q1.getResultList().get(0);
				query =	"SELECT new ru.futurelink.mo.orm.HistoryResult(d.mId, d.mModifyDate, d.mDescription, d.mObjectId) FROM WorkLogSupport d " + 
						"WHERE d.mObjectClassName = :objectClassName AND " +
						"d.mObjectId = :objectId "+
						"order by d.mModifyDate desc";

				q2 = mSession.persistent().getPersistentManager().getEm().createQuery(query, getDataClass());
				q2.setParameter("objectClassName", getDataClass().getName());
				q2.setParameter("objectId", objectId);
			}
		}
		
 		if ((q2 != null) && (q2.getResultList().size() > 0)) {
			logger().debug("Количество элементов: {} ", q2.getResultList().size());
			mList.addObjectList((List<? extends CommonObject>) q2.getResultList());
		}
		setDTO(mList);
	}

	@Override
	protected void doBeforeCreateComposite() {
		params().add("tableClass", HistoryListTable.class);
		params().add("listComposite", HistoryListComposite.class);
		params().add("itemDialogTitle", "history");
	}

	// Глушим даблклик по строке истории
	@Override
	public void handleItemDoubleClicked(CommonDTO data) {}

	@Override
	protected void doBeforeInit() {
		
	}

	@Override
	protected void doAfterInit() {
		
	}

	@Override
	public void handleDataQueryExecuted() {}

}

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

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.ViewerDTO;
import ru.futurelink.mo.orm.dto.ViewerDTOList;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IHistoryObject;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.SimpleListController;

public class HistoryListController extends SimpleListController {

	private Long	mCode;
	
	private ViewerDTOList<ViewerDTO> mList;

	public HistoryListController(CompositeController parentController,
			Class<? extends ICommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	public HistoryListController(CompositeController parentController,
			Class<? extends ICommonObject> dataClass, CompositeParams compositeParams) {
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
		if (IHistoryObject.class.isAssignableFrom(getDataClass())) {
			query =	"SELECT new ru.futurelink.mo.orm.HistoryResult(d.id, d.mModifyDate, d.mWorkLog.mDescription, d.id, d.mAuthor) FROM " + 
				getDataClass().getSimpleName()+" d " +
				"where d.owner = :creator and d.mCode.id = :code " +
				"order by d.mModifyDate desc";

			q2 = mSession.persistent().getEm().createQuery(query, getDataClass());
			q2.setParameter("creator", getSession().getDatabaseUser());
			q2.setParameter("code", mCode);
		} else {
			// Найдем ID того объекта, для которого будем выбирать историю из лога
			TypedQuery<String> q1 = mSession.persistent().getEm().createQuery(
					"SELECT d.id FROM " +
					getDataClass().getSimpleName()+" d "+
					"WHERE d.owner = :creator AND d.mCode.id = :code", String.class);
			q1.setParameter("creator", getSession().getDatabaseUser());
			q1.setParameter("code", mCode);			
			if (q1.getResultList().size() > 0) {
				String objectId = q1.getResultList().get(0);
				query =	"SELECT new ru.futurelink.mo.orm.HistoryResult(d.id, d.mModifyDate, d.mDescription, d.mObjectId, d.mAuthor) "
						+ "FROM WorkLogSupport d " 
						+ "WHERE d.mObjectClassName = :objectClassName AND "
						+ "d.mObjectId = :objectId "
						+ "order by d.mModifyDate desc";

				q2 = mSession.persistent().getEm().createQuery(query, getDataClass());
				q2.setParameter("objectClassName", getDataClass().getName());
				q2.setParameter("objectId", objectId);
			}
		}
		
 		if ((q2 != null) && (q2.getResultList().size() > 0)) {
			logger().debug("Количество элементов: {} ", q2.getResultList().size());
			mList.addObjectList((List<? extends ModelObject>) q2.getResultList());
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
	public void handleItemDoubleClicked(IDTO data) {}

	@Override
	protected void doBeforeInit() {
		
	}

	@Override
	protected void doAfterInit() {
		
	}

	@Override
	public void handleDataQueryExecuted() {}

}

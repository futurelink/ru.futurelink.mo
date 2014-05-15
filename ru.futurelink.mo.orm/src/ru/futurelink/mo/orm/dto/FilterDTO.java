package ru.futurelink.mo.orm.dto;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Query;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.types.DateRange;

public class FilterDTO extends CommonDTO {
	private static final long serialVersionUID = 1L;

	private Map<String, ArrayList<Object>> mQueryConditions;
	private HashMap<String, Object>			mAdditionalValues;
	private String							mConditionsQueryString = "";

	public FilterDTO() {
		super(null);
		
		mQueryConditions = new HashMap<String, ArrayList<Object>>() ;
		mAdditionalValues = new HashMap<String, Object>();		
	}

	@Override
	public Object getDataField(String fieldName, String fieldGetterName,
			String fieldSetterName) throws DTOException {
		return getCondition(fieldName); 
	}

	@Override
	public void setDataField(String fieldName, String fieldGetterName,
			String fieldSetterName, Object value) {
		if (fieldName == null) return;
		if (value != null) {
			System.out.println("FilterDTO does field "+fieldName+" set to "+value+" with "+fieldSetterName);
			addCondition(fieldName, value);
		} else {
			System.out.println("FilterDTO does field "+fieldName+" removed condition with "+fieldSetterName);
			removeCondition(fieldName);
		}
	}

	private Object getCondition(String fieldName) {
		if (mQueryConditions.get(fieldName) != null) {
			return mQueryConditions.get(fieldName).get(0);
		} else {
			return null;
		}
	}

	private void addCondition(String fieldName, Object fieldValue) {
		// Пустое значение отменяет фильтр по этому полю
		if (fieldValue == null || fieldName.equals("")) {
			removeCondition(fieldName);
		} else {
			if (!mQueryConditions.containsKey(fieldName))
				mQueryConditions.put(fieldName, new ArrayList<Object>());		
			mQueryConditions.get(fieldName).add(fieldValue);
			
			calculateConditions();
		}
	}

	private void removeCondition(String fieldName) {
		if (mQueryConditions.containsKey(fieldName))
			mQueryConditions.remove(fieldName);

		calculateConditions();
	}

	@Override
	public void clear() {
		mQueryConditions.clear();
		mAdditionalValues.clear();
		mConditionsQueryString = "";
		mData = null;
	}	

	private void calculateConditions() {
		mConditionsQueryString = "";
		mAdditionalValues.clear();	// Зануляем условия перед пересчетом

		ArrayList<String> additionalConditions = new ArrayList<String>();
		if (!mQueryConditions.isEmpty()) {
			int k = 0;
			for (String fieldName : mQueryConditions.keySet()) {
				k++;
				String cond = ""; 
				for (int n = 0; n < mQueryConditions.get(fieldName).size(); n++) {
					// Пустые условия мы не учитываем при формировании списка условий
					// просто их пропускаем.
					if (mQueryConditions.get(fieldName).get(n) != null) {
						// If condition is a CommonDTO object - use data item specific value processing
						if (CommonDTO.class.isAssignableFrom(mQueryConditions.get(fieldName).get(n).getClass())) {
							try {
								String id = ((CommonDTO)mQueryConditions.get(fieldName).get(n)).getId();
								cond = cond + fieldName + ".mId = :fieldData" + k + n;
								mAdditionalValues.put("fieldData" + k + n, id);
							} catch (DTOException ex) {
								// TODO handle this error
								ex.printStackTrace();
							}
						} else {
							// If condition is a string or any other value - use simple string processing
							if (!mQueryConditions.get(fieldName).get(n).equals("")) {
								// Обработка диапазона дат "от" и "до" 
								if (DateRange.class.isAssignableFrom(mQueryConditions.get(fieldName).get(n).getClass())) {
									cond = cond + "(" + fieldName + " >= :fieldData" + k + n+"min and "+
										fieldName + " <= :fieldData" + k + n+"max)";
									mAdditionalValues.put("fieldData" + k + n+"min", 
										((DateRange)mQueryConditions.get(fieldName).get(n)).getBeginDate());						
									mAdditionalValues.put("fieldData" + k + n+"max", 
										((DateRange)mQueryConditions.get(fieldName).get(n)).getEndDate());						
								}
							} else {					
								cond = cond + fieldName + " = :fieldData" + k + n;
								mAdditionalValues.put("fieldData" + k + n, mQueryConditions.get(fieldName).get(n));
							}
						}

						// Если условий по одному полю несколько, то добавляем "or"
						if (n < mQueryConditions.get(fieldName).size()-1)
							cond = cond + " or ";
					}
				}
				if ((cond != null) && !cond.isEmpty()) {
					cond = "("+ cond +")";
					additionalConditions.add(cond);
				}
			}

			if (additionalConditions.size() > 0)
				mConditionsQueryString = mConditionsQueryString + " and " + join(additionalConditions, " and ");
			
			System.out.println("Допонительные условия для выбора из списка: " + mConditionsQueryString);			
		}
	}

	/**
	 * Получить список условий фильтра как часть выражения WHERE.
	 * @return
	 */
	public String getConditionsAsQuery(String tableAlias) {
		return mConditionsQueryString;
	}

	public static String join(AbstractCollection<String> s, String delimiter) {
		if (s == null || s.isEmpty()) return "";
		Iterator<String> iter = s.iterator();
		StringBuilder builder = new StringBuilder(iter.next());
		while( iter.hasNext() ) {
			builder.append(delimiter).append(iter.next());
		}

		return builder.toString();
	}

	/**
	 * Установить параметры для запроса.
	 * 
	 * @param q
	 */
	public void setConditionsParameters(Query q) {
		if ((mAdditionalValues != null) && (mAdditionalValues.size() > 0)) {
			for (String key : mAdditionalValues.keySet()) {
				if (key != null && !mAdditionalValues.get(key).equals(""))
					q.setParameter(key, mAdditionalValues.get(key));
			}
		}
	}
	
	@Override
	public void refresh() {
		
	}

	/**
	 * @param string
	 * @return
	 */
	public boolean haveCondition(String key) {
		return mQueryConditions.containsKey(key);
	}
}

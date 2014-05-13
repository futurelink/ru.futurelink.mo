package ru.futurelink.mo.web.composites.fields;

import ru.futurelink.mo.orm.dto.FilterDTO;

public interface FilterListener {
	public void filterChanged(FilterDTO filterDTO);
}

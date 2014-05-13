/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import java.util.ArrayList;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.exceptions.ValidationException;
import ru.futurelink.mo.web.controller.RelatedController;

/**
 * @author pavlov
 *
 */
public interface IItemController 
	extends IController {

	/* Методы для работы с DTO */

	public CommonDTO getDTO();
	public void setDTO(CommonDTO dto)  throws DTOException;

	/* Методы манипуляции объъектом */

	public void create() throws DTOException;
	public void openById(String id) throws OpenException;
	public void open(CommonObject data) throws OpenException;
	public void save() throws SaveException, DTOException, ValidationException;
	public void close();

	public void revertChanges() throws DTOException;

	/* События об операциях с объектом */

	public void doAfterCreate();	
	public void doAfterOpen() throws OpenException;	
	public void doAfterSave() throws SaveException;

	/* Разные другие методы */

	public ArrayList<String> getDataChanged() throws DTOException;
	public boolean getRelatedDataChanged();

	public void addRelatedController(RelatedController ctrl);
}

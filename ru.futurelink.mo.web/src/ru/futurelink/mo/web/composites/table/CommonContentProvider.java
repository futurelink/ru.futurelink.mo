package ru.futurelink.mo.web.composites.table;

import java.util.HashSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * Провайдер контента всегда возвращает массив объектов,
 * в конкретном случае - это преобразованный в массив список ArrayList<String>.
 * 
 * Провайдер не отображает элементы с deleteFlag > 0. Нужно учитывать, что для использования
 * этого провайдера на вход setInput надо передавать ArrayList<? extends CommonDTO, иначе будет
 * ClassCastException.
 * 
 * @author Futurelink
 *
 */
public class CommonContentProvider  implements IStructuredContentProvider {
	private static final long serialVersionUID = 1L;

	private Object[] 			elements;
	private HashSet<CommonDTO>	undisplayed;
	private int				displayedCount;

	public Object[] getElements( Object inputElement ) { return elements; }

	// Когда выполняется setInput на таблице, к которой привязан экземпляр этого
	// класса, вызывается этот метод.
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		undisplayed = new HashSet<CommonDTO>();
		displayedCount = 0;
		
		if(newInput == null) {
			// Возвращается пустой массив, если
			// если передать нулевой input.
	   		elements = new CommonDTO[0];
	   	} else {
			@SuppressWarnings("unchecked")
			CommonDTOList<? extends CommonDTO> newElements = (CommonDTOList<? extends CommonDTO>) newInput;
			countDisplayableElements(newElements);
	   		
	   		// Преобразуем объект данных, которые переданы в массив,
			// при этом обеспечиваем упорядочение!
	   		elements = new CommonDTO[displayedCount];
	   		if (displayedCount > 0) {
	   			int i = 0;
	   			for (String id : newElements.getOrderList().values()) {
	   				CommonDTO element = newElements.getDTOList().get(id);
	   				if (element != null) {
	   					if (!undisplayed.contains(element)) {
	   						elements[i] = element;
	   						i++;
	   					}
	   				}
	   			}
	   		}
	    }		
	}

	// Посчитать отображаемые элементы
	private synchronized void countDisplayableElements(CommonDTOList<? extends CommonDTO> newElements) {
		int counter = 0;
		for (String id : newElements.getOrderList().values()) {
			CommonDTO element = newElements.getDTOList().get(id);
			if (element != null) {
				try {
					if (getIsDisplayable(element))
						counter++;
					else 
						undisplayed.add(element);
				} catch (DTOAccessException ex) {
					System.out.println(ex.getMessage());				
					undisplayed.add(element);
				} catch (DTOException ex) {
					System.out.println(ex.getMessage());
					undisplayed.add(element);
				}
			}
		}
		displayedCount = counter;
	}
	
    public void dispose() {
    	elements = null;
    }
    
    /**
     * Метод определения надо ли показывать элемент в таблице. Нужно переопределить
     * если метод определения отличается от анализа deleteFlag.
     * 
     * @param element
     * @return
     * @throws DTOException
     */
    protected boolean getIsDisplayable(CommonDTO element) throws DTOException {
    	return !(Boolean) element.getDeleteFlag();
    }
}

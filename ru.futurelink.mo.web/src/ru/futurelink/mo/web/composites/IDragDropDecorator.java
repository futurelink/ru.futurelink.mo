/**
 * 
 */
package ru.futurelink.mo.web.composites;

import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * @author pavlov
 *
 */
public interface IDragDropDecorator {
	/**
	 * Добавить возможность перетаскивания из композита.
	 * В самом композите этот метод ничего не делает, однако его нужно переопределить
	 * на наследуемых классах, чтобы добавить функциональность.
	 * 
	 * Например, если композит содержит TableViewer из которого возможно перетягивание, то
	 * нужно выполнить addDragSupport для него.
	 * 
	 * @param operations
	 * @param transferTypes
	 * @param listener
	 */
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener);
	
	/**
	 * Добавить поддержу приема драг-н-дропа в композит.
	 * В самом композите этот метод ничего не делает, однако его нужно переопределить
	 * на наследуемых классах, чтобы добавить функциональность.
	 * 
	 * Например, если композит содержит TableViewer в который возможно перетягивание, то
	 * нужно выполнить addDragSupport для него.
	 * 
	 * @param operations
	 * @param transferTypes
	 * @param listener
	 */
	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener);
}

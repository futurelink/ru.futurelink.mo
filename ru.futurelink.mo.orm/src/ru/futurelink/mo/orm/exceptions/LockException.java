package ru.futurelink.mo.orm.exceptions;

import java.util.Date;

/**
 * Исключение вызванное попыткой открыть на редактирование заблокированный
 * элемент данных.
 * 
 * @author pavlov_d
 *
 */
public final class LockException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Тип исключения: 
	 * 0 - блокировка уже установлена при вызове уствновки, 
	 * 1 - блокировки нет при вызове снятия блокировки
	 */
	public Short lockExceptionType;
	
	/**
	 * Время, когда была установлена блокировка.
	 */
	public Date lockSetTime;
	
	/**
	 * Пользователь, который установил блокировку.
	 */
	public String lockUserName;
}

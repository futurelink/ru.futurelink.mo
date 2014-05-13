package ru.futurelink.mo.web.controller;

/**
 * Обработчик событий происходящих на элементах подключенных
 * к контроллеру. Обработчик необходимо переопределить для любого
 * нестандартного контроллера, добавив методы обработки событий.
 * 
 * @author pavlov_d
 *
 */
public interface CommonControllerListener {
	public void sendError(String errorText, Exception exception);
}

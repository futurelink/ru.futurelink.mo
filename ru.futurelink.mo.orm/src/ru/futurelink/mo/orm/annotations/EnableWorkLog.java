package ru.futurelink.mo.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * По этой аннотации определяется будет ли необходимо создавать
 * журнал действий пользователя по элементу данных. Аннотация
 * применяется к классу Entity.
 * 
 * @author Futurelink
 *
 */
@Target(value=ElementType.TYPE)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface EnableWorkLog {

}

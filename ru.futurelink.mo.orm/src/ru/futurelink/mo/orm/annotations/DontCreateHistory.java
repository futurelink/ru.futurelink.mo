package ru.futurelink.mo.orm.annotations;

import java.lang.annotation.*;

@Target(value=ElementType.FIELD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface DontCreateHistory {

}

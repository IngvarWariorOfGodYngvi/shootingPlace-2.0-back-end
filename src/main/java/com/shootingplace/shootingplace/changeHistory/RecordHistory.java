package com.shootingplace.shootingplace.changeHistory;

import com.shootingplace.shootingplace.history.HistoryEntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordHistory {

    String action();

    HistoryEntityType entity();

    /**
     * Index argumentu metody,
     * z którego pobieramy UUID encji
     * (np. memberUUID = 0)
     */
    int entityArgIndex() default -1;
}


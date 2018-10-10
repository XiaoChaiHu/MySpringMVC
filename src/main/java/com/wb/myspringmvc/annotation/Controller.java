package com.wb.myspringmvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)//类上
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * controller名称
     * @return
     */
    String value();
}

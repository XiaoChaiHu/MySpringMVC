package com.wb.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * 依赖注入
 */
@Documented
@Target(ElementType.FIELD)//字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    String value();
}

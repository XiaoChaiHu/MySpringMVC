package com.wb.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * url映射地址
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})//能使用到类上和方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();
}

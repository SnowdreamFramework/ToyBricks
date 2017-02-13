package com.github.snowdream.toybricks.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by snowdream on 17/2/10.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Implementation {
     Class value();

     boolean global() default false;

     boolean singleton() default false;
}

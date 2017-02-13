package com.github.snowdream.toybricks.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by snowdream on 17/2/11.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Interface {
}

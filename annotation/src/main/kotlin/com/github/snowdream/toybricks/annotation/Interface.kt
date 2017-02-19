package com.github.snowdream.toybricks.annotation

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by snowdream on 17/2/11.
 */
@Retention(RetentionPolicy.CLASS)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Interface

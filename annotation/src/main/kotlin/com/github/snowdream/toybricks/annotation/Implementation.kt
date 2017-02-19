package com.github.snowdream.toybricks.annotation

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * Created by snowdream on 17/2/10.
 */
@Retention(RetentionPolicy.CLASS)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Implementation(val value: KClass<*>, val global: Boolean = false, val singleton: Boolean = false)

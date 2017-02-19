package com.github.snowdream.toybricks.annotation

import proguard.annotation.KeepImplementations
import proguard.annotation.KeepName

/**
 * Created by snowdream on 17/2/9.
 */
@KeepName
@KeepImplementations
interface InterfaceLoader {

    /**
     * get Implementation for the interface.
     */
    fun <T> getImplementation(clazz: Class<T>): T
}

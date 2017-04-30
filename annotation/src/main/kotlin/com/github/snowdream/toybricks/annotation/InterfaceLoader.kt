package com.github.snowdream.toybricks.annotation

/**
 * Created by snowdream on 17/2/9.
 */
interface InterfaceLoader {

    /**
     * get Implementation for the interface.
     */
    fun <T> getImplementation(clazz: Class<T>): T?
}

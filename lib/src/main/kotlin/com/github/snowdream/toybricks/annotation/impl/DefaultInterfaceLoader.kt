package com.github.snowdream.toybricks.annotation.impl

import com.github.snowdream.toybricks.annotation.InterfaceLoader

/**
 * Created by snowdream on 17/2/10.
 */
class DefaultInterfaceLoader : InterfaceLoader {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementation(clazz: Class<T>): T? {
        var implementation: T? = null

        val implClass: Class<T>

        val pn = clazz.`package`.name
        val sn = clazz.simpleName

        val className: String
        if (sn.startsWith("Interface")) {
            className = pn + ".impl." + sn + "Impl"
        } else {
            className = pn + ".impl." + upperName(sn.substring(1)) + "Impl"
        }

        try {
            implClass = Class.forName(className) as Class<T>

            implementation = implClass.newInstance() as T
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }

        return implementation
    }

    /**
     * change the first character to UpperCase
     */
    private fun upperName(name: String): String {
        val upperName = name.substring(0, 1).toUpperCase() + name.substring(1)
        return upperName
    }
}

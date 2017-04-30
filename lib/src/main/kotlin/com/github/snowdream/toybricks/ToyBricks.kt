package com.github.snowdream.toybricks

import com.github.snowdream.toybricks.annotation.InterfaceLoader
import com.github.snowdream.toybricks.annotation.impl.DefaultInterfaceLoader

/**
 * Created by snowdream on 17/2/10.
 */
class ToyBricks {

        companion object{
            //Default InterfaceLoader
            @JvmField var sDefaultInterfaceLoader: InterfaceLoader = DefaultInterfaceLoader()

            //NewI InterfaceLoader
            @JvmField var sNewInterfaceLoader: InterfaceLoader ?= sDefaultInterfaceLoader.getImplementation(InterfaceLoader::class.java)

            @JvmStatic @Synchronized fun <T> getImplementation(clazz: Class<T>): T {
                var implementation: T?

                implementation = sNewInterfaceLoader?.getImplementation(clazz)

                if (implementation == null) {
                    implementation = sDefaultInterfaceLoader.getImplementation(clazz)
                }

                return implementation!!
            }
        }
}
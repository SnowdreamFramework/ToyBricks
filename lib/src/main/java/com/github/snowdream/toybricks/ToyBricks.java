package com.github.snowdream.toybricks;

import com.github.snowdream.toybricks.annotation.InterfaceLoader;
import com.github.snowdream.toybricks.annotation.impl.DefaultInterfaceLoader;

/**
 * Created by snowdream on 17/2/10.
 */
public class ToyBricks {

    //Default InterfaceLoader
    private static InterfaceLoader sDefaultInterfaceLoader;

    //NewI InterfaceLoader
    private static InterfaceLoader sNewInterfaceLoader;

    public static synchronized <T> T getImplementation(Class<T> clazz) {
        T implementation = null;

        if (sDefaultInterfaceLoader == null){
            sDefaultInterfaceLoader = new DefaultInterfaceLoader();
        }

        if (sNewInterfaceLoader == null){
            sNewInterfaceLoader = sDefaultInterfaceLoader.getImplementation(InterfaceLoader.class);
        }

        if (sNewInterfaceLoader != null){
            implementation = sNewInterfaceLoader.getImplementation(clazz);
        }

        if (implementation == null){
            implementation = sDefaultInterfaceLoader.getImplementation(clazz);
        }

        return implementation;
    }
}
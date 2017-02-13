package com.github.snowdream.toybricks.annotation.impl;

import com.github.snowdream.toybricks.annotation.InterfaceLoader;

import java.util.HashMap;

/**
 * Created by snowdream on 17/2/10.
 * <p>
 * generate it with apt
 */
class NewInterfaceLoader implements InterfaceLoader {
    private static HashMap<Class, Object> sSingletonMap = new HashMap<>();
    private static HashMap<Class, Class> sGlobalMap = new HashMap<>();
    private static HashMap<Class, Class> sDefaultMap = new HashMap<>();

    public NewInterfaceLoader() {
        addGlobalMap();
        addDefaultMap();
        addSingletonMap();
    }

    private void addGlobalMap() {
        //sGlobalMap.put()
    }

    private void addDefaultMap() {
        //sDefaultMap.put()
    }

    private void addSingletonMap() {
        //sSingletonMap.put()
    }

    @Override
    public <T> T getImplementation(Class<T> clazz) {
        T implementation = null;

        boolean isSingleton = false;

        Class implClass;

        implClass = sGlobalMap.get(clazz);

        if (implClass == null) {
            implClass = sDefaultMap.get(clazz);
        }

        if (implClass != null) {
            isSingleton = sSingletonMap.containsKey(implClass);

            if (isSingleton) {
                implementation = (T) sSingletonMap.get(implClass);
                if (implementation != null) {
                    return implementation;
                }
            }

            try {
                implementation = (T) implClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (isSingleton && implementation != null) {
                sSingletonMap.put(implClass, implementation);
            }
        }


        return implementation;
    }
}

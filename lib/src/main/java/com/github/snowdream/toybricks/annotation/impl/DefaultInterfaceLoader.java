package com.github.snowdream.toybricks.annotation.impl;

import com.github.snowdream.toybricks.annotation.InterfaceLoader;

/**
 * Created by snowdream on 17/2/10.
 */
public class DefaultInterfaceLoader implements InterfaceLoader {

    @Override
    public <T> T getImplementation(Class<T> clazz) {
        T implementation = null;

        Class<T> implClass;

        String pn = clazz.getPackage().getName();
        String sn = clazz.getSimpleName();

        String className;
        if (sn.startsWith("Interface")){
            className = pn + ".impl." + sn + "Impl";
        }else{
            className = pn + ".impl." + upperName(sn.substring(1)) + "Impl";
        }

        try {
            implClass = (Class<T>) Class.forName(className);

            implementation = (T) implClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return implementation;
    }

    /**
     * change the first character to UpperCase
     */
    private String upperName(String name) {
        String upperName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return  upperName;
    }
}

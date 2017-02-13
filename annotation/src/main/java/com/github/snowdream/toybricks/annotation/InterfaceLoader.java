package com.github.snowdream.toybricks.annotation;

import proguard.annotation.KeepImplementations;
import proguard.annotation.KeepName;

/**
 * Created by snowdream on 17/2/9.
 */
@KeepName
@KeepImplementations
public interface InterfaceLoader {

    <T> T getImplementation(Class<T> clazz);
}

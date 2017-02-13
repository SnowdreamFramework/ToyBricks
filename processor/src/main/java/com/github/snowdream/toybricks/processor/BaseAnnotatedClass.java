package com.github.snowdream.toybricks.processor;

import javax.lang.model.element.TypeElement;

/**
 * Created by snowdream on 17/2/12.
 */
public abstract class BaseAnnotatedClass {

    protected TypeElement annotatedClassElement;

    /**
     *
     * @return
     */
    public abstract void checkValid(ProcessorManager processorManager) throws ProcessingException;
}

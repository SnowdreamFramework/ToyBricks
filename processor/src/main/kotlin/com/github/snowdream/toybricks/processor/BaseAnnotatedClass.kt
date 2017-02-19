package com.github.snowdream.toybricks.processor

import javax.lang.model.element.TypeElement

/**
 * Created by snowdream on 17/2/12.
 */
abstract class BaseAnnotatedClass {

    protected lateinit var annotatedClassElement: TypeElement

    /**

     */
    @Throws(ProcessingException::class)
    abstract fun checkValid(processorManager: ProcessorManager)
}

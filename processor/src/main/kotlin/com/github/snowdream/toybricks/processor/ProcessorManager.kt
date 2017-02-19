package com.github.snowdream.toybricks.processor

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import java.util.ArrayList

/**
 * Created by snowdream on 17/2/12.
 */
class ProcessorManager internal constructor(private val mProcessingEnvironment: ProcessingEnvironment) : Handler {
    private var mHandlers: MutableList<BaseContainerHandler>? = null

    val typeUtils: Types
    val elementUtils: Elements
    val filer: Filer
    val messager: Messager


    init {

        typeUtils = mProcessingEnvironment.typeUtils
        elementUtils = mProcessingEnvironment.elementUtils
        filer = mProcessingEnvironment.filer
        messager = mProcessingEnvironment.messager
    }

    internal fun addHandlers(vararg containerHandlers: BaseContainerHandler) {
        if (containerHandlers == null || containerHandlers.size <= 0) return

        for (handler in containerHandlers) {

            if (mHandlers == null) {
                mHandlers = ArrayList<BaseContainerHandler>()
            }

            if (!mHandlers!!.contains(handler)) {
                mHandlers!!.add(handler)
            }
        }
    }

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        if (mHandlers != null) {
            for (handler in mHandlers!!) {
                handler.handle(processorManager, roundEnvironment)
            }
        }

    }

    /**
     * Prints an error message

     * @param e The element which has caused the error. Can be null
     * *
     * @param msg The error message
     */
    fun error(e: Element?, msg: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e)
    }

    /**
     * Prints an info message

     * @param e The element which has caused the error. Can be null
     * *
     * @param msg The error message
     */
    fun info(e: Element, msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e)
    }
}

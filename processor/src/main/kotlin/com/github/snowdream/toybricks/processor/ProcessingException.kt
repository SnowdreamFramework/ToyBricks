package com.github.snowdream.toybricks.processor

import javax.lang.model.element.Element

/**
 * Created by snowdream on 17/2/12.
 */
class ProcessingException(element: Element, msg: String, vararg args: String?) : Exception(String.format(msg, *args)) {

    var element: Element
        internal set

    init {
        this.element = element
    }
}

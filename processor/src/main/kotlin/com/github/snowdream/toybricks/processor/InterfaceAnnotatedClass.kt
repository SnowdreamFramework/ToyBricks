package com.github.snowdream.toybricks.processor

import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by snowdream on 17/2/12.
 */
class InterfaceAnnotatedClass @Throws(ProcessingException::class)
constructor(classElement: TypeElement) : BaseAnnotatedClass() {

    init {
        this.annotatedClassElement = classElement
    }

    /**
     * The original element that was annotated with @Implementation
     */
    val typeElement: TypeElement
        get() = annotatedClassElement

    @Throws(ProcessingException::class)
    override fun checkValid(processorManager: ProcessorManager) {

        // Cast to TypeElement, has more type specific methods
        val classElement = annotatedClassElement

        if (!classElement.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(classElement, "The class %s is not public.",
                    classElement.qualifiedName.toString())
        }
    }
}

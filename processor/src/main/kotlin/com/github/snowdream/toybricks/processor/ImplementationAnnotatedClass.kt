package com.github.snowdream.toybricks.processor

import com.github.snowdream.toybricks.annotation.Implementation

import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Created by snowdream on 17/2/12.
 */
class ImplementationAnnotatedClass @Throws(ProcessingException::class)
constructor(classElement: TypeElement) : BaseAnnotatedClass() {

    /**
     * Get the full qualified name of the type specified in  [Implementation.value].

     * @return qualified name
     */
    var qualifiedInterfaceClassName: String? = null
        private set
    /**
     * Get the simple name of the type specified in  [Implementation.value].

     * @return qualified name
     */
    var simpleInterfaceName: String? = null
        private set

    var isGolbal = false
    var isSingleton = false

    init {
        this.annotatedClassElement = classElement
        val annotation = classElement.getAnnotation(Implementation::class.java)


        isGolbal = annotation.global

        isSingleton = annotation.singleton

        // Get the full QualifiedTypeName
        try {
            val interfaceClass = annotation.value
            qualifiedInterfaceClassName = interfaceClass.qualifiedName
            simpleInterfaceName = interfaceClass.simpleName
        } catch (mte: MirroredTypeException) {
            val classTypeMirror = mte.typeMirror as DeclaredType
            val classTypeElement = classTypeMirror.asElement() as TypeElement
            qualifiedInterfaceClassName = classTypeElement.qualifiedName.toString()
            simpleInterfaceName = classTypeElement.simpleName.toString()
        }

    }

    /**
     * The original element that was annotated with @Implementation
     */
    val typeElement: TypeElement
        get() = annotatedClassElement

    @Throws(ProcessingException::class)
    override fun checkValid(processorManager: ProcessorManager) {
        val typeUtils = processorManager.typeUtils
        val elementUtils = processorManager.elementUtils

        // Cast to TypeElement, has more type specific methods
        val classElement = annotatedClassElement

        if (!classElement.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(classElement, "The class %s is not public.",
                    classElement.qualifiedName.toString())
        }

        // Check if it's an abstract class
        if (classElement.modifiers.contains(Modifier.ABSTRACT)) {
            throw ProcessingException(classElement,
                    "The class %s is abstract. You can't annotate abstract classes with @%s",
                    classElement.qualifiedName.toString(), Implementation::class.java.simpleName)
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        val superClassElement = elementUtils.getTypeElement(qualifiedInterfaceClassName)
        if (superClassElement.kind == ElementKind.INTERFACE) {
            // Check interface implemented
            if (!classElement.interfaces.contains(superClassElement.asType())) {
                throw ProcessingException(classElement,
                        "The class %s annotated with @%s must implement the interface %s",
                        classElement.qualifiedName.toString(), Implementation::class.java.simpleName,
                        qualifiedInterfaceClassName)
            }
        } else {
            // Check subclassing
            var currentClass = classElement
            while (true) {
                val superClassType = currentClass.superclass

                if (superClassType.kind == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    throw ProcessingException(classElement,
                            "The class %s annotated with @%s must inherit from %s",
                            classElement.qualifiedName.toString(), Implementation::class.java.simpleName,
                            qualifiedInterfaceClassName)
                }

                if (superClassType.toString() == qualifiedInterfaceClassName) {
                    // Required super class found
                    break
                }

                // Moving up in inheritance tree
                currentClass = typeUtils.asElement(superClassType) as TypeElement
            }
        }

        // Check if an empty public constructor is given
        for (enclosed in classElement.enclosedElements) {
            if (enclosed.kind == ElementKind.CONSTRUCTOR) {
                val constructorElement = enclosed as ExecutableElement
                if (constructorElement.parameters.size == 0 && constructorElement.modifiers
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return
                }
            }
        }

        // No empty constructor found
        throw ProcessingException(classElement,
                "The class %s must provide an public empty default constructor",
                classElement.qualifiedName.toString())
    }
}

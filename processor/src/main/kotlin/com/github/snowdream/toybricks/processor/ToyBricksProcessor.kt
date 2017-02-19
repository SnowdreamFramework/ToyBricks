package com.github.snowdream.toybricks.processor

import com.github.snowdream.toybricks.annotation.Implementation
import com.github.snowdream.toybricks.annotation.Interface
import com.google.auto.service.AutoService

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import java.util.LinkedHashSet

/**
 * Created by snowdream on 17/2/11.
 */
@AutoService(Processor::class)
class ToyBricksProcessor : AbstractProcessor() {

    private lateinit var manager: ProcessorManager

    /**
     * If the processor class is annotated with [ ], return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.

     * @return the names of the annotation types supported by this
     * * processor, or an empty set if none
     */
    override fun getSupportedAnnotationTypes(): Set<String> {
        val supportedTypes = LinkedHashSet<String>()
        supportedTypes.add(Interface::class.java.canonicalName)
        supportedTypes.add(Implementation::class.java.canonicalName)
        return supportedTypes
    }

    /**
     * If the processor class is annotated with [ ], return the source version in the
     * annotation.  If the class is not so annotated, [ ][SourceVersion.RELEASE_6] is returned.

     * @return the latest source version supported by this processor
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    @Synchronized override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        manager = ProcessorManager(processingEnv)
        manager.addHandlers(
                InterfaceImplementationHandler()
        )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        manager.handle(manager, roundEnv)

        // return true if we successfully processed the Annotation.
        return true
    }
}

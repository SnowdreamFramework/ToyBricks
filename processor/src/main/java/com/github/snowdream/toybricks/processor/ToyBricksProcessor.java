package com.github.snowdream.toybricks.processor;

import com.github.snowdream.toybricks.annotation.Implementation;
import com.github.snowdream.toybricks.annotation.Interface;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by snowdream on 17/2/11.
 */
@AutoService(Processor.class)
public class ToyBricksProcessor extends AbstractProcessor {

    private ProcessorManager manager;

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedAnnotationTypes}, return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.
     *
     * @return the names of the annotation types supported by this
     * processor, or an empty set if none
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(Interface.class.getCanonicalName());
        supportedTypes.add(Implementation.class.getCanonicalName());
        return supportedTypes;
    }

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedSourceVersion}, return the source version in the
     * annotation.  If the class is not so annotated, {@link
     * javax.lang.model.SourceVersion#RELEASE_6} is returned.
     *
     * @return the latest source version supported by this processor
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        manager = new ProcessorManager(processingEnv);
        manager.addHandlers(
                new InterfaceImplementationHandler()
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        manager.handle(manager, roundEnv);

        // return true if we successfully processed the Annotation.
        return true;
    }
}

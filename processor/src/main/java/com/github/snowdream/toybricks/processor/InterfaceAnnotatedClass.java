package com.github.snowdream.toybricks.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by snowdream on 17/2/12.
 */
public class InterfaceAnnotatedClass extends BaseAnnotatedClass {

    public InterfaceAnnotatedClass(TypeElement classElement) throws ProcessingException {
        this.annotatedClassElement = classElement;
    }

    /**
     * The original element that was annotated with @Implementation
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    @Override
    public void checkValid(ProcessorManager processorManager) throws ProcessingException {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = annotatedClassElement;

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
        }
    }
}

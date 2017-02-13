package com.github.snowdream.toybricks.processor;

import com.github.snowdream.toybricks.annotation.Implementation;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by snowdream on 17/2/12.
 */
public class ImplementationAnnotatedClass extends BaseAnnotatedClass{

    private String qualifiedInterfaceClassName;
    private String simpleInterfaceName;

    private boolean isGolbal = false;
    private boolean isSingleton = false;

    public ImplementationAnnotatedClass(TypeElement classElement) throws ProcessingException {
        this.annotatedClassElement = classElement;
        Implementation annotation = classElement.getAnnotation(Implementation.class);


        isGolbal = annotation.global();

        isSingleton = annotation.singleton();

        // Get the full QualifiedTypeName
        try {
            Class interfaceClass = annotation.value();
            qualifiedInterfaceClassName = interfaceClass.getCanonicalName();
            simpleInterfaceName = interfaceClass.getSimpleName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedInterfaceClassName = classTypeElement.getQualifiedName().toString();
            simpleInterfaceName = classTypeElement.getSimpleName().toString();
        }
    }

    /**
     * Get the full qualified name of the type specified in  {@link Implementation#value()}.
     *
     * @return qualified name
     */
    public String getQualifiedInterfaceClassName() {
        return qualifiedInterfaceClassName;
    }

    /**
     * Get the simple name of the type specified in  {@link Implementation#value()}.
     *
     * @return qualified name
     */
    public String getSimpleInterfaceName() {
        return simpleInterfaceName;
    }

    public boolean isGolbal(){
        return  isGolbal;
    }

    public boolean isSingleton(){
        return isSingleton;
    }

    /**
     * The original element that was annotated with @Implementation
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    @Override
    public void checkValid(ProcessorManager processorManager) throws ProcessingException {
        Types typeUtils = processorManager.getTypeUtils();
        Elements elementUtils = processorManager.getElementUtils();

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = annotatedClassElement;

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(classElement,
                    "The class %s is abstract. You can't annotate abstract classes with @%s",
                    classElement.getQualifiedName().toString(), Implementation.class.getSimpleName());
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        TypeElement superClassElement =
                elementUtils.getTypeElement(getQualifiedInterfaceClassName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            // Check interface implemented
            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
                throw new ProcessingException(classElement,
                        "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Implementation.class.getSimpleName(),
                        getQualifiedInterfaceClassName());
            }
        } else {
            // Check subclassing
            TypeElement currentClass = classElement;
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    throw new ProcessingException(classElement,
                            "The class %s annotated with @%s must inherit from %s",
                            classElement.getQualifiedName().toString(), Implementation.class.getSimpleName(),
                            getQualifiedInterfaceClassName());
                }

                if (superClassType.toString().equals(getQualifiedInterfaceClassName())) {
                    // Required super class found
                    break;
                }

                // Moving up in inheritance tree
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return;
                }
            }
        }

        // No empty constructor found
        throw new ProcessingException(classElement,
                "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
    }
}

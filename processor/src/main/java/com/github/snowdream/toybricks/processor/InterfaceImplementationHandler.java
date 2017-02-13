package com.github.snowdream.toybricks.processor;

import com.github.snowdream.toybricks.annotation.Implementation;
import com.github.snowdream.toybricks.annotation.Interface;
import com.github.snowdream.toybricks.annotation.InterfaceLoader;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.*;

/**
 * Created by snowdream on 17/2/12.
 */
public class InterfaceImplementationHandler extends BaseContainerHandler {
    private Map<String, InterfaceAnnotatedClass> interfaceMap =
            new HashMap<>();

    private Map<String, ImplementationAnnotatedClass> globalImplementationMap =
            new HashMap<>();

    private Map<String, ImplementationAnnotatedClass> defaultImplementationMap =
            new HashMap<>();

    private Set<ImplementationAnnotatedClass> singletonImplementationSet =
            new HashSet<>();

    private ProcessorManager processorManager = null;

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        this.processorManager = processorManager;

        interfaceMap.clear();
        globalImplementationMap.clear();
        defaultImplementationMap.clear();
        singletonImplementationSet.clear();

        handleInterfaceAnnotation(roundEnvironment);
        handleImplementationAnnotation(roundEnvironment);

        checkInterfaceImplementation();
        generateJavaFile();
    }

    /**
     * handle Annotation @Interface
     */
    private void handleInterfaceAnnotation(RoundEnvironment roundEnv) {
        try {
            // Scan classes
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Interface.class)) {

                // Check if a class has been annotated with @Interface
                if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                    throw new ProcessingException(annotatedElement, "Only Interface can be annotated with @%s",
                            Interface.class.getSimpleName());
                }

                // We can cast it, because we know that it of ElementKind.INTERFACE
                TypeElement typeElement = (TypeElement) annotatedElement;

                InterfaceAnnotatedClass annotatedClass = new InterfaceAnnotatedClass(typeElement);

                annotatedClass.checkValid(processorManager);

                if (!interfaceMap.containsKey(annotatedClass.getTypeElement().getQualifiedName().toString())) {
                    interfaceMap.put(annotatedClass.getTypeElement().getQualifiedName().toString(), annotatedClass);

                    processorManager.info(annotatedClass.getTypeElement(), "Found interface " +
                            annotatedClass.getTypeElement().getQualifiedName().toString() +
                            ", annotated with @" + Interface.class.getSimpleName());
                }
            }
        } catch (ProcessingException e) {
            processorManager.error(e.getElement(), e.getMessage());
        }
    }

    /**
     * handle Annotation @Implementation
     */
    private void handleImplementationAnnotation(RoundEnvironment roundEnv) {

        try {
            // Scan classes
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Implementation.class)) {

                // Check if a class has been annotated with @Factory
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(annotatedElement, "Only classes can be annotated with @%s",
                            Implementation.class.getSimpleName());
                }

                // We can cast it, because we know that it of ElementKind.CLASS
                TypeElement typeElement = (TypeElement) annotatedElement;

                ImplementationAnnotatedClass annotatedClass = new ImplementationAnnotatedClass(typeElement);

                annotatedClass.checkValid(processorManager);

                String interfaceName = annotatedClass.getQualifiedInterfaceClassName();

                if (annotatedClass.isGolbal()) {
                    if (globalImplementationMap.containsKey(interfaceName)) {
                        ImplementationAnnotatedClass implementationAnnotatedClass = globalImplementationMap.get(interfaceName);

                        // Alredy existing
                        throw new ProcessingException(annotatedClass.getTypeElement(),
                                "Conflict: The class %s is annotated with @%s with interface %s, but %s already uses the same interface",
                                annotatedClass.getTypeElement().getQualifiedName().toString(), Implementation.class.getSimpleName(),
                                interfaceName, implementationAnnotatedClass.getTypeElement().getQualifiedName().toString());
                    } else {
                        globalImplementationMap.put(interfaceName, annotatedClass);

                        if (annotatedClass.isSingleton()){
                            singletonImplementationSet.add(annotatedClass);
                        }

                        processorManager.info(annotatedClass.getTypeElement(), "Found Global Implementation " +
                                annotatedClass.getTypeElement().getQualifiedName().toString() +
                                ", annotated with @" + Implementation.class.getSimpleName());
                    }
                } else {
                    if (defaultImplementationMap.containsKey(interfaceName)) {
                        ImplementationAnnotatedClass implementationAnnotatedClass = defaultImplementationMap.get(interfaceName);

                        // Alredy existing
                        throw new ProcessingException(annotatedClass.getTypeElement(),
                                "Conflict: The class %s is annotated with @%s with interface %s, but %s already uses the same interface",
                                annotatedClass.getTypeElement().getQualifiedName().toString(), Implementation.class.getSimpleName(),
                                interfaceName, implementationAnnotatedClass.getTypeElement().getQualifiedName().toString());
                    } else {
                        defaultImplementationMap.put(interfaceName, annotatedClass);

                        if (annotatedClass.isSingleton()){
                            singletonImplementationSet.add(annotatedClass);
                        }

                        processorManager.info(annotatedClass.getTypeElement(), "Found Default Implementation " +
                                annotatedClass.getTypeElement().getQualifiedName().toString() +
                                ", annotated with @" + Implementation.class.getSimpleName());
                    }
                }
            }
        } catch (ProcessingException e) {
            processorManager.error(e.getElement(), e.getMessage());
        }
    }

    /**
     * check whether interface has implementation.
     */
    private void checkInterfaceImplementation() {
        if (interfaceMap.isEmpty()) {
            return;
        }

        Iterator iterator = interfaceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, InterfaceAnnotatedClass> entry = (Map.Entry<String, InterfaceAnnotatedClass>) iterator.next();
            String interfaceName = entry.getKey();
            InterfaceAnnotatedClass annotatedClass = entry.getValue();

            boolean hasImplementation;

            if (!globalImplementationMap.isEmpty()) {
                hasImplementation = globalImplementationMap.get(interfaceName) != null;

                if (hasImplementation) continue;
            }

            if (!defaultImplementationMap.isEmpty()) {
                hasImplementation = defaultImplementationMap.get(interfaceName) != null;

                if (hasImplementation) continue;
            }

            processorManager.error(annotatedClass.getTypeElement(), "Error: The interface " +
                    annotatedClass.getTypeElement().getQualifiedName().toString() +
                    " has no Implementation.");
        }
    }


    /**
     * generate java file
     */
    private void generateJavaFile() {
        if (interfaceMap.isEmpty() &&
                globalImplementationMap.isEmpty() &&
                defaultImplementationMap.isEmpty()){
            return;
        }


        Elements elementUtils = processorManager.getElementUtils();
        Filer filer = processorManager.getFiler();

        Class clazz = InterfaceLoader.class;

        String pn = clazz.getPackage().getName();
        String sn = clazz.getSimpleName();

        String packageName = pn + ".impl";
        String className = sn + "Impl";

        try {
            //private static HashMap<Class, Object> sSingletonMap = new HashMap<Class, Object>();
            TypeVariableName classTypeVariableName= TypeVariableName.get("Class");
            TypeVariableName objectTypeVariableName= TypeVariableName.get("Object");

            ParameterizedTypeName singletonMapParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), classTypeVariableName,objectTypeVariableName);

            FieldSpec singletonMap = FieldSpec.builder(singletonMapParameterizedTypeName,"sSingletonMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new $T()", singletonMapParameterizedTypeName)
                    .build();

            // private static HashMap<Class, Class> sGlobalMap= new HashMap<>();
            ParameterizedTypeName implementationParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), classTypeVariableName,classTypeVariableName);

            FieldSpec globalMap = FieldSpec.builder(implementationParameterizedTypeName,"sGlobalMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new $T()", implementationParameterizedTypeName)
                    .build();

            // private static HashMap<Class, Class> sDefaultMap= new HashMap<>();
            FieldSpec defaultMap = FieldSpec.builder(implementationParameterizedTypeName,"sDefaultMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new $T()", implementationParameterizedTypeName)
                    .build();


            // public NewInterfaceLoader()
            MethodSpec constructorMethodSpec = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("addGlobalMap()")
                    .addStatement("addDefaultMap()")
                    .addStatement("addSingletonMap()")
                    .build();

            // private void addGlobalMap()
            MethodSpec.Builder globalMapMethodSpecBuilder = MethodSpec.methodBuilder("addGlobalMap")
                    .addModifiers(Modifier.PRIVATE);

            Iterator iterator = globalImplementationMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ImplementationAnnotatedClass> entry = (Map.Entry<String, ImplementationAnnotatedClass>) iterator.next();
                String interfaceName = entry.getKey();
                ImplementationAnnotatedClass annotatedClass = entry.getValue();

                globalMapMethodSpecBuilder.addStatement("sGlobalMap.put($L.class,$L.class)",interfaceName,annotatedClass.getTypeElement().getQualifiedName());
            }

            MethodSpec globalMapMethodSpec = globalMapMethodSpecBuilder.build();

            // private void addDefaultMap()
            MethodSpec.Builder defaultMapMethodSpecBuilder = MethodSpec.methodBuilder("addDefaultMap")
                    .addModifiers(Modifier.PRIVATE);

            iterator = defaultImplementationMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ImplementationAnnotatedClass> entry = (Map.Entry<String, ImplementationAnnotatedClass>) iterator.next();
                String interfaceName = entry.getKey();
                ImplementationAnnotatedClass annotatedClass = entry.getValue();

                defaultMapMethodSpecBuilder.addStatement("sDefaultMap.put($L.class,$L.class)",interfaceName,annotatedClass.getTypeElement().getQualifiedName());
            }

            MethodSpec defaultMapMethodSpec = defaultMapMethodSpecBuilder.build();


            // private void addSingletonMap()
            MethodSpec.Builder singletonMapMethodSpecBuilder = MethodSpec.methodBuilder("addSingletonMap")
                    .addModifiers(Modifier.PRIVATE);

            Iterator<ImplementationAnnotatedClass> it = singletonImplementationSet.iterator();
            while (it.hasNext()) {
                ImplementationAnnotatedClass annotatedClass = it.next();

                singletonMapMethodSpecBuilder.addStatement("sSingletonMap.put($L.class,null)",annotatedClass.getTypeElement().getQualifiedName());
            }

            MethodSpec singletonMapMethodSpec = singletonMapMethodSpecBuilder.build();

            //public <T> T getImplementation(Class<T> clazz)
            TypeVariableName typeVariableName = TypeVariableName.get("T");
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName);

            MethodSpec getImplementationMethodSpec = MethodSpec.methodBuilder("getImplementation")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeVariableName)
                    .addTypeVariable(typeVariableName)
                    .addParameter(parameterizedTypeName,"clazz")
                    .addStatement("T implementation = null")
                    .addCode("\n")
                    .addStatement("boolean isSingleton = false")
                    .addCode("\n")
                    .addStatement("Class implClass")
                    .addCode("\n")
                    .addStatement("implClass = sGlobalMap.get(clazz)")
                    .addCode("\n")
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("if (implClass == null)")
                            .addStatement("implClass = sDefaultMap.get(clazz)")
                            .endControlFlow()
                            .build())
                    .addCode("\n")
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("if (implClass != null)")
                            .addStatement("isSingleton = sSingletonMap.containsKey(implClass)")
                            .add("\n")
                            .add(CodeBlock.builder()
                                    .beginControlFlow("if (isSingleton)")
                                    .addStatement("implementation = (T) sSingletonMap.get(implClass)")
                                    .add(CodeBlock.builder()
                                            .beginControlFlow("if (implementation != null)")
                                            .addStatement("return implementation")
                                            .endControlFlow()
                                            .build())
                                    .endControlFlow()
                                    .build())
                            .add("\n")
                            .add(CodeBlock.builder()
                                    .beginControlFlow("try")
                                    .addStatement("implementation = (T) implClass.newInstance()")
                                    .nextControlFlow("catch (InstantiationException e)")
                                    .addStatement("e.printStackTrace()")
                                    .nextControlFlow("catch (IllegalAccessException e)")
                                    .addStatement("e.printStackTrace()")
                                    .endControlFlow()
                                    .build()
                            )
                            .add("\n")
                            .add(CodeBlock.builder()
                                    .beginControlFlow("if (isSingleton && implementation != null)")
                                    .addStatement("sSingletonMap.put(implClass, implementation)")
                                    .endControlFlow()
                                    .build()
                            )
                            .endControlFlow()
                            .build())
                    .addCode("\n")
                    .addStatement("return implementation")
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder(className)//HelloWorld是类名
                    .addJavadoc("\n")
                    .addJavadoc("Created by snowdream")
                    .addJavadoc("\n")
                    .addJavadoc("\n")
                    .addJavadoc("This file is automatically generated by apt(Annotation Processing Tool)")
                    .addJavadoc("\n")
                    .addJavadoc("Do not modify this file -- YOUR CHANGES WILL BE ERASED!")
                    .addJavadoc("\n")
                    .addJavadoc("\n")
                    .addJavadoc("This file should *NOT* be checked into Version Control Systems,")
                    .addJavadoc("\n")
                    .addJavadoc("as it contains information specific to your local configuration.")
                    .addJavadoc("\n")
                    .addModifiers(Modifier.FINAL)
                    .addSuperinterface(InterfaceLoader.class)
                    .addField(singletonMap)
                    .addField(globalMap)
                    .addField(defaultMap)
                    .addMethod(constructorMethodSpec)
                    .addMethod(globalMapMethodSpec)
                    .addMethod(defaultMapMethodSpec)
                    .addMethod(singletonMapMethodSpec)
                    .addMethod(getImplementationMethodSpec)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            processorManager.error(null, e.getMessage());
        }
    }

}

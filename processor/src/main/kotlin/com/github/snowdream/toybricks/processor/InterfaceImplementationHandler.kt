package com.github.snowdream.toybricks.processor

import com.github.snowdream.toybricks.annotation.Implementation
import com.github.snowdream.toybricks.annotation.Interface
import com.github.snowdream.toybricks.annotation.InterfaceLoader
import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by snowdream on 17/2/12.
 */
class InterfaceImplementationHandler : BaseContainerHandler() {
    private val interfaceMap = mutableMapOf<String, InterfaceAnnotatedClass>()

    private val globalImplementationMap = mutableMapOf<String, ImplementationAnnotatedClass>()

    private val defaultImplementationMap = mutableMapOf<String, ImplementationAnnotatedClass>()

    private val singletonImplementationSet = mutableSetOf<ImplementationAnnotatedClass>()

    private lateinit var processorManager: ProcessorManager

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        this.processorManager = processorManager

        interfaceMap.clear()
        globalImplementationMap.clear()
        defaultImplementationMap.clear()
        singletonImplementationSet.clear()

        handleInterfaceAnnotation(roundEnvironment)
        handleImplementationAnnotation(roundEnvironment)

        checkInterfaceImplementation()
        generateJavaFile()
    }

    /**
     * handle Annotation @Interface
     */
    private fun handleInterfaceAnnotation(roundEnv: RoundEnvironment) {
        try {
            // Scan classes
            for (annotatedElement in roundEnv.getElementsAnnotatedWith(Interface::class.java)) {

                // Check if a class has been annotated with @Interface
                if (annotatedElement.kind != ElementKind.INTERFACE) {
                    throw ProcessingException(annotatedElement, "Only Interface can be annotated with @%s",
                            Interface::class.java.simpleName)
                }

                // We can cast it, because we know that it of ElementKind.INTERFACE
                val typeElement = annotatedElement as TypeElement

                val annotatedClass = InterfaceAnnotatedClass(typeElement)

                annotatedClass.checkValid(processorManager)

                if (!interfaceMap.containsKey(annotatedClass.typeElement.qualifiedName.toString())) {
                    interfaceMap.put(annotatedClass.typeElement.qualifiedName.toString(), annotatedClass)

                    processorManager.info(annotatedClass.typeElement, "Found interface " +
                            annotatedClass.typeElement.qualifiedName.toString() +
                            ", annotated with @" + Interface::class.java.simpleName)
                }
            }
        } catch (e: ProcessingException) {
            processorManager.error(e.element, e.message as String)
        }

    }

    /**
     * handle Annotation @Implementation
     */
    private fun handleImplementationAnnotation(roundEnv: RoundEnvironment) {

        try {
            // Scan classes
            for (annotatedElement in roundEnv.getElementsAnnotatedWith(Implementation::class.java)) {

                // Check if a class has been annotated with @Factory
                if (annotatedElement.kind != ElementKind.CLASS) {
                    throw ProcessingException(annotatedElement, "Only classes can be annotated with @%s",
                            Implementation::class.java.simpleName)
                }

                // We can cast it, because we know that it of ElementKind.CLASS
                val typeElement = annotatedElement as TypeElement

                val annotatedClass = ImplementationAnnotatedClass(typeElement)

                annotatedClass.checkValid(processorManager)

                val interfaceName = annotatedClass.qualifiedInterfaceClassName

                if (annotatedClass.isGolbal) {
                    if (globalImplementationMap.containsKey(interfaceName)) {
                        val implementationAnnotatedClass = globalImplementationMap[interfaceName]

                        // Alredy existing
                        throw ProcessingException(annotatedClass.typeElement,
                                "Conflict: The class %s is annotated with @%s with interface %s, but %s already uses the same interface",
                                annotatedClass.typeElement.qualifiedName.toString(), Implementation::class.java.simpleName,
                                interfaceName, implementationAnnotatedClass?.typeElement?.qualifiedName.toString())
                    } else {
                        globalImplementationMap.put(interfaceName as String, annotatedClass)

                        if (annotatedClass.isSingleton) {
                            singletonImplementationSet.add(annotatedClass)
                        }

                        processorManager.info(annotatedClass.typeElement, "Found Global Implementation " +
                                annotatedClass.typeElement.qualifiedName.toString() +
                                ", annotated with @" + Implementation::class.java.simpleName)
                    }
                } else {
                    if (defaultImplementationMap.containsKey(interfaceName)) {
                        val implementationAnnotatedClass = defaultImplementationMap[interfaceName]

                        // Alredy existing
                        throw ProcessingException(annotatedClass.typeElement,
                                "Conflict: The class %s is annotated with @%s with interface %s, but %s already uses the same interface",
                                annotatedClass.typeElement.qualifiedName.toString(), Implementation::class.java.simpleName,
                                interfaceName, implementationAnnotatedClass?.typeElement?.qualifiedName.toString())
                    } else {
                        defaultImplementationMap.put(interfaceName as String, annotatedClass)

                        if (annotatedClass.isSingleton) {
                            singletonImplementationSet.add(annotatedClass)
                        }

                        processorManager.info(annotatedClass.typeElement, "Found Default Implementation " +
                                annotatedClass.typeElement.qualifiedName.toString() +
                                ", annotated with @" + Implementation::class.java.simpleName)
                    }
                }
            }
        } catch (e: ProcessingException) {
            processorManager.error(e.element, e.message as String)
        }

    }

    /**
     * check whether interface has implementation.
     */
    private fun checkInterfaceImplementation() {
        if (interfaceMap.isEmpty()) {
            return
        }

        val iterator = interfaceMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val interfaceName = entry.key
            val annotatedClass = entry.value

            var hasImplementation: Boolean

            if (!globalImplementationMap.isEmpty()) {
                hasImplementation = globalImplementationMap[interfaceName] != null

                if (hasImplementation) continue
            }

            if (!defaultImplementationMap.isEmpty()) {
                hasImplementation = defaultImplementationMap[interfaceName] != null

                if (hasImplementation) continue
            }

            processorManager.error(annotatedClass.typeElement, "Error: The interface " +
                    annotatedClass.typeElement.getQualifiedName().toString() +
                    " has no Implementation.")
        }
    }


    /**
     * generate java file
     */
    private fun generateJavaFile() {
        if (interfaceMap.isEmpty() &&
                globalImplementationMap.isEmpty() &&
                defaultImplementationMap.isEmpty()) {
            return
        }


        //val elementUtils = processorManager.elementUtils
        val filer = processorManager.filer

        val clazz = InterfaceLoader::class.java

        val pn = clazz.`package`.name
        val sn = clazz.simpleName

        val packageName = pn + ".impl"
        val className = sn + "Impl"

        try {
            //private static HashMap<Class, Object> sSingletonMap = new HashMap<Class, Object>();
            val classTypeVariableName = TypeVariableName.get("Class")
            val objectTypeVariableName = TypeVariableName.get("Object")
            val hashMapClzName = ClassName.get(HashMap::class.java)


            val singletonMapParameterizedTypeName = ParameterizedTypeName.get(hashMapClzName, classTypeVariableName, objectTypeVariableName)

            val singletonMap = FieldSpec.builder(singletonMapParameterizedTypeName, "sSingletonMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", singletonMapParameterizedTypeName)
                    .build()

            // private static HashMap<Class, Class> sGlobalMap= new HashMap<>();
            val implementationParameterizedTypeName = ParameterizedTypeName.get(hashMapClzName, classTypeVariableName, classTypeVariableName)

            val globalMap = FieldSpec.builder(implementationParameterizedTypeName, "sGlobalMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", implementationParameterizedTypeName)
                    .build()

            // private static HashMap<Class, Class> sDefaultMap= new HashMap<>();
            val defaultMap = FieldSpec.builder(implementationParameterizedTypeName, "sDefaultMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", implementationParameterizedTypeName)
                    .build()


            // public NewInterfaceLoader()
            val constructorMethodSpec = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("addGlobalMap()")
                    .addStatement("addDefaultMap()")
                    .addStatement("addSingletonMap()")
                    .build()

            // private void addGlobalMap()
            val globalMapMethodSpecBuilder = MethodSpec.methodBuilder("addGlobalMap")
                    .addModifiers(Modifier.PRIVATE)

            var iterator = globalImplementationMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val interfaceName = entry.key
                val annotatedClass = entry.value

                globalMapMethodSpecBuilder.addStatement("sGlobalMap.put(\$L.class,\$L.class)", interfaceName, annotatedClass.typeElement.getQualifiedName())
            }

            val globalMapMethodSpec = globalMapMethodSpecBuilder.build()

            // private void addDefaultMap()
            val defaultMapMethodSpecBuilder = MethodSpec.methodBuilder("addDefaultMap")
                    .addModifiers(Modifier.PRIVATE)

            iterator = defaultImplementationMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val interfaceName = entry.key
                val annotatedClass = entry.value

                defaultMapMethodSpecBuilder.addStatement("sDefaultMap.put(\$L.class,\$L.class)", interfaceName, annotatedClass.typeElement.getQualifiedName())
            }

            val defaultMapMethodSpec = defaultMapMethodSpecBuilder.build()


            // private void addSingletonMap()
            val singletonMapMethodSpecBuilder = MethodSpec.methodBuilder("addSingletonMap")
                    .addModifiers(Modifier.PRIVATE)

            val it = singletonImplementationSet.iterator()
            while (it.hasNext()) {
                val annotatedClass = it.next()

                singletonMapMethodSpecBuilder.addStatement("sSingletonMap.put(\$L.class,null)", annotatedClass.typeElement.qualifiedName)
            }

            val singletonMapMethodSpec = singletonMapMethodSpecBuilder.build()

            //public <T> T getImplementation(Class<T> clazz)
            val typeVariableName = TypeVariableName.get("T")
            val classClzName = ClassName.get(Class::class.java)

            val parameterizedTypeName = ParameterizedTypeName.get(classClzName, typeVariableName)
            val getImplementationMethodSpec = MethodSpec.methodBuilder("getImplementation")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeVariableName)
                    .addTypeVariable(typeVariableName)
                    .addParameter(parameterizedTypeName, "clazz")
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
                    .build()

            val typeSpec = TypeSpec.classBuilder(className)//HelloWorld是类名
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
                    .addSuperinterface(InterfaceLoader::class.java)
                    .addField(singletonMap)
                    .addField(globalMap)
                    .addField(defaultMap)
                    .addMethod(constructorMethodSpec)
                    .addMethod(globalMapMethodSpec)
                    .addMethod(defaultMapMethodSpec)
                    .addMethod(singletonMapMethodSpec)
                    .addMethod(getImplementationMethodSpec)
                    .build()

            val javaFile = JavaFile.builder(packageName, typeSpec)
                    .build()
            javaFile.writeTo(filer)
        } catch (e: IOException) {
            e.printStackTrace()
            processorManager.error(null, e.message as String)
        }

    }

}

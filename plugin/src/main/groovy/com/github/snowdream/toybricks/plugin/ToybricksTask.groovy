package com.github.snowdream.toybricks.plugin

import com.github.snowdream.toybricks.annotation.Implementation
import com.squareup.javapoet.*
import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.StopExecutionException

import com.github.snowdream.toybricks.annotation.InterfaceLoader

import javax.lang.model.element.Modifier
import java.util.zip.ZipFile

/**
 * Created by snowdream on 17/3/8.
 * @author snowdream
 * @date 2017/03/08
 */
class ToybricksTask extends DefaultTask {
    private static final String ToyBricksJsonFileName = "ToyBricks.json"

    @Input
    boolean isKotlinAndroidProject

    /**
     * The input dependencies.
     */
    @InputFiles
    @SkipWhenEmpty
    FileCollection dependencies

    /**
     * The output directory.
     */
    @OutputDirectories
    FileCollection outputDirs

    List<ToyBricksJsonEntity> list = new ArrayList<>()

    ToyBricksJsonEntity merged = new ToyBricksJsonEntity()

    @TaskAction
    main() {
        initMergedToyBricksJson()
        parseToyBricksJson()
        checkToyBricksJson()
        mergeToyBricksJson()
        generateJavaFile()
    }

    def initMergedToyBricksJson() {
        merged.interfaceList = new ArrayList<>()
        merged.singletonImplementation = new ArrayList<>()
        merged.globalImplementation = new HashMap<>()
        merged.defaultImplementation = new HashMap<>()
    }

    def parseToyBricksJson() {
        //parse current project ToyBricks.json
        def jsonSlurper = new JsonSlurper()

        def currentEntity
        outputDirs.each { File outputDir ->
            if (currentEntity == null) {
                def currentToyBricksJson = new File(outputDir, "ToyBricks.json")

                if (currentToyBricksJson.exists() && currentToyBricksJson.canRead()) {
                    currentEntity = jsonSlurper.parse(currentToyBricksJson) as ToyBricksJsonEntity
                }
            }
        }

        if (currentEntity != null){
            list.add(currentEntity)
        }else{
            println "There is no ToyBricks.json in the current project."
        }

        //parse dependencies ToyBricks.json
        dependencies.each { File file ->
            String content = getToyBricksJsonContentFromZip(file)
            //println content

            if (content != null && !content.isEmpty()) {
                try {
                    def entity = jsonSlurper.parseText(content) as ToyBricksJsonEntity
                    entity.library = file
                    list.add(entity)
                } catch (JsonException exception) {
                    exception.printStackTrace()
                }
            }
        }

        //merge interface
        if (!list.isEmpty()){
            for(ToyBricksJsonEntity entity : list){
                for (String interfaceName: entity.interfaceList){
                    if (interfaceName == null || interfaceName == "") {
                        continue
                    }

                    if (merged.interfaceList.contains(interfaceName)){
                        continue
                    }

                    merged.interfaceList.add(interfaceName)
                }
            }
        }
    }

    def checkToyBricksJson(){
        def interfaceList = merged.interfaceList

        if (interfaceList.isEmpty()){
            return
        }

        for (String interfaceName : interfaceList){
            def hasImplementation = false
            def hasGlobalImplementation = false
            def globalImplementation
            def hasDefaultImplementation = false
            def defaultImplementation

            for(ToyBricksJsonEntity entity : list){

                for (Map.Entry<String, String> entry : entity.globalImplementation.entrySet()) {
                    def _interfaceName = entry.key
                    def _implementation = entry.value

                    if (interfaceName.equalsIgnoreCase(_interfaceName) && _implementation != null && _implementation != ""){
                        if (hasGlobalImplementation){
                            throw new StopExecutionException(
                                    "Conflict: The class ${_implementation} is annotated with @${Implementation.class.simpleName} with interface ${interfaceName}, but ${globalImplementation} already uses the same interface")
                        }else{
                            hasImplementation = true
                            hasGlobalImplementation = true
                            globalImplementation = _implementation
                        }
                    }
                }

                for (Map.Entry<String, String> entry : entity.defaultImplementation.entrySet()) {
                    def _interfaceName = entry.key
                    def _implementation = entry.value

                    if (interfaceName.equalsIgnoreCase(_interfaceName) && _implementation != null && _implementation != ""){
                        if (hasDefaultImplementation){
                            throw new StopExecutionException(
                                    "Conflict: The class ${_implementation} is annotated with @${Implementation.class.simpleName} with interface ${interfaceName}, but ${defaultImplementation} already uses the same interface")
                        }else{
                            hasImplementation = true
                            hasDefaultImplementation = true
                            defaultImplementation = _implementation
                        }
                    }
                }
            }

            if (!hasImplementation){
                throw new StopExecutionException(
                        "Error: The interface  ${interfaceName} has no Implementation.")
            }
        }
    }


    def mergeToyBricksJson() {
        for(ToyBricksJsonEntity entity : list){
            merged.globalImplementation.putAll(entity.globalImplementation)
            merged.defaultImplementation.putAll(entity.defaultImplementation)
            merged.singletonImplementation.addAll(entity.singletonImplementation)
        }
    }

    /**
     * generate java file
     */
    def generateJavaFile() {
        def interfaceList = merged.interfaceList
        def globalImplementationMap = merged.globalImplementation
        def defaultImplementationMap = merged.defaultImplementation
        def singletonImplementationList = merged.singletonImplementation

        if (interfaceList.isEmpty() &&
                globalImplementationMap.isEmpty() &&
                defaultImplementationMap.isEmpty()) {
            return
        }

        def clazz = InterfaceLoader.class
        def pn =clazz.package.name
        def sn = clazz.simpleName

        def packageName = pn + ".impl"
        def className = sn + "Impl"

        try {
            //private static HashMap<Class, Object> sSingletonMap = new HashMap<Class, Object>();
            def classTypeVariableName = TypeVariableName.get("Class")
            def objectTypeVariableName = TypeVariableName.get("Object")
            def hashMapClzName = ClassName.get(HashMap.class)


            def singletonMapParameterizedTypeName = ParameterizedTypeName.get(hashMapClzName, classTypeVariableName, objectTypeVariableName)

            def singletonMap = FieldSpec.builder(singletonMapParameterizedTypeName, "sSingletonMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", singletonMapParameterizedTypeName)
                    .build()

            // private static HashMap<Class, Class> sGlobalMap= new HashMap<>();
            def implementationParameterizedTypeName = ParameterizedTypeName.get(hashMapClzName, classTypeVariableName, classTypeVariableName)

            def globalMap = FieldSpec.builder(implementationParameterizedTypeName, "sGlobalMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", implementationParameterizedTypeName)
                    .build()

            // private static HashMap<Class, Class> sDefaultMap= new HashMap<>();
            def defaultMap = FieldSpec.builder(implementationParameterizedTypeName, "sDefaultMap")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .initializer("new \$T()", implementationParameterizedTypeName)
                    .build()

            // public NewInterfaceLoader()
            def constructorMethodSpec = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("addGlobalMap()")
                    .addStatement("addDefaultMap()")
                    .addStatement("addSingletonMap()")
                    .build()

            // private void addGlobalMap()
            def globalMapMethodSpecBuilder = MethodSpec.methodBuilder("addGlobalMap")
                    .addModifiers(Modifier.PRIVATE)

            for (Map.Entry<String, String> entry : globalImplementationMap.entrySet()) {
                def interfaceName = entry.key
                def qualifiedName = entry.value

                globalMapMethodSpecBuilder.addStatement("sGlobalMap.put(\$L.class,\$L.class)", interfaceName, qualifiedName)
            }


            def globalMapMethodSpec = globalMapMethodSpecBuilder.build()

            // private void addDefaultMap()
            def defaultMapMethodSpecBuilder = MethodSpec.methodBuilder("addDefaultMap")
                    .addModifiers(Modifier.PRIVATE)

            for (Map.Entry<String, String> entry : defaultImplementationMap.entrySet()) {
                def interfaceName = entry.key
                def qualifiedName = entry.value

                defaultMapMethodSpecBuilder.addStatement("sDefaultMap.put(\$L.class,\$L.class)", interfaceName, qualifiedName)
            }

            def defaultMapMethodSpec = defaultMapMethodSpecBuilder.build()

            // private void addSingletonMap()
            def singletonMapMethodSpecBuilder = MethodSpec.methodBuilder("addSingletonMap")
                    .addModifiers(Modifier.PRIVATE)

            for (String qualifiedName : singletonImplementationList){
                singletonMapMethodSpecBuilder.addStatement("sSingletonMap.put(\$L.class,null)", qualifiedName)
            }

            def singletonMapMethodSpec = singletonMapMethodSpecBuilder.build()

            //public <T> T getImplementation(Class<T> clazz)
            def typeVariableName = TypeVariableName.get("T")
            def classClzName = ClassName.get(Class.class)

            def parameterizedTypeName = ParameterizedTypeName.get(classClzName, typeVariableName)
            def getImplementationMethodSpec = MethodSpec.methodBuilder("getImplementation")
                    .addAnnotation(Override.class)
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

            def typeSpec = TypeSpec.classBuilder(className)//HelloWorld是类名
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
                    .build()

            def javaFile = JavaFile.builder(packageName, typeSpec)
                    .build()

            outputDirs.each { File outputDir ->
                javaFile.writeTo(outputDir)
                println "${typeSpec.name}.java  has been generated in ${outputDir}."
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    /**
     * get ToyBricksJson Content From AAR or JAR
     *
     * @param file
     * @return
     */
    static String getToyBricksJsonContentFromZip(File file) {
        //println file.absolutePath

        def content = ""

        if (file.exists() && file.canRead()){
            def zipFile = new ZipFile(file)

            zipFile.entries().findAll { ToyBricksJsonFileName.equalsIgnoreCase(it.name) }.each {
                content = zipFile.getInputStream(it).text
            }
        }
        return content
    }
}
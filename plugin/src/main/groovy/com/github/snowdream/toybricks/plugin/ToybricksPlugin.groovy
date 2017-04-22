package com.github.snowdream.toybricks.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Created by snowdream on 17/3/8.
 * @author snowdream
 * @date 2017/03/08
 */
class ToybricksPlugin implements Plugin<Project> {
    private static final String PLUGIN_ANDROID_APP = "com.android.application"
    private static final String PLUGIN_ANDROID_LIBRARY = "com.android.library"
    private static final String PLUGIN_ANDROID_KOTLIN = "kotlin-android"

    private static final int CONFIGURATION_TYPE_APT = 0 //java
    private static final int CONFIGURATION_TYPE_KAPT = 1 //kotlin


    private Project project

    private int type = CONFIGURATION_TYPE_APT

    void apply(Project project) {
        this.project = project

        checkEnvironment()

        checkConfigurationType()

        if (hasPlugin(PLUGIN_ANDROID_APP)){
            createAndroidAppTasks()
        }

        if (hasPlugin(PLUGIN_ANDROID_LIBRARY)) {
            createAndroidLibraryTasks()
        }
    }

    /**
     * check environment
     */
    def checkEnvironment() {
        if (!hasPlugin(PLUGIN_ANDROID_APP) && !hasPlugin(PLUGIN_ANDROID_LIBRARY) ) {
            throw new StopExecutionException(
                    "Must be applied after 'com.android.application' or 'com.android.library' plugin.")
        }
    }

    /**
     * check configuration type
     */
    def checkConfigurationType() {
        if (!project.configurations.annotationProcessor.isEmpty()){
            type = CONFIGURATION_TYPE_APT
        }else if (!project.configurations.kapt.isEmpty()){
            type = CONFIGURATION_TYPE_KAPT
        }else {
            type = CONFIGURATION_TYPE_APT
        }
    }

    /**
     * hasPlugin
     *
     * @param name plugin name
     * @return
     */
    private boolean hasPlugin(String name) {
        return project.plugins.hasPlugin(name)
    }

    /**
     * create tasks for android app
     *
     * @param project
     * @return
     */
    def createAndroidAppTasks() {
        project.android.applicationVariants.all { variant ->
            def variantCapitalizeName = "${variant.name}".capitalize()

            //see: https://bitbucket.org/qbusict/android-gradle-scripts/src/60394ba8f5efa64badb17c9388f38b89f09c5d9a/annotations.groovy
            def aptOutputDir
            if (type == CONFIGURATION_TYPE_KAPT) {
                aptOutputDir = project.file(new File("${project.buildDir}", "generated/source/kapt/${variant.name}"))
            }else{
                aptOutputDir = project.file(new File("${project.buildDir}", "generated/source/apt/${variant.dirName}"))
            }

            project.android.sourceSets["${variant.name}"].java.srcDirs += aptOutputDir.getPath()

            project.task("process${variantCapitalizeName}ToyBricksJsonFile", type: ToybricksTask) {
                description "Merge ToyBricks.json from dependencies into one, Then generate java source file: InterfaceLoaderImpl.java."

                dependencies = project.configurations.compile

                isKotlinAndroidProject = hasPlugin(PLUGIN_ANDROID_KOTLIN)

                if (type == CONFIGURATION_TYPE_KAPT) {
                    outputDirs = project.files(new File("${project.buildDir}", "generated/source/kapt/${variant.name}"))
                }else{
                    outputDirs = project.files(new File("${project.buildDir}", "generated/source/apt/${variant.dirName}"))
                }
            }

            project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"].dependsOn(project.tasks["compile${variantCapitalizeName}JavaWithJavac"])
            project.tasks["compile${variantCapitalizeName}JavaWithJavac"].finalizedBy(project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"])

            Task task = project.tasks["compile${variantCapitalizeName}JavaWithJavac"]
            if (task != null && task instanceof JavaCompile) {
                JavaCompile javaCompileTask = task as JavaCompile

                project.task("compile${variantCapitalizeName}ToyBricksGeneratedSources", type: JavaCompile) {
                    destinationDir javaCompileTask.getDestinationDir()
                    sourceCompatibility javaCompileTask.getSourceCompatibility()
                    targetCompatibility javaCompileTask.getTargetCompatibility()
                    classpath = variant.javaCompile.classpath
                    classpath += javaCompileTask.getOutputs().getFiles()
                    dependencyCacheDir javaCompileTask.getDependencyCacheDir()
                    //options = javaCompileTask.getOptions()
                    toolChain = javaCompileTask.getToolChain()
                    includes = javaCompileTask.getIncludes()
                    excludes = javaCompileTask.getExcludes()
                    //source = variant.javaCompile.source
                    source = aptOutputDir.getPath()
                }

                project.tasks["compile${variantCapitalizeName}ToyBricksGeneratedSources"].dependsOn(project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"])
                project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"].finalizedBy(project.tasks["compile${variantCapitalizeName}ToyBricksGeneratedSources"])

                project.tasks["transformClassesWithDexFor${variantCapitalizeName}"].dependsOn(project.tasks["compile${variantCapitalizeName}ToyBricksGeneratedSources"])
            }

//            def isKotlinAndroidProject = hasPlugin(PLUGIN_ANDROID_KOTLIN)
//            if (isKotlinAndroidProject){
//                project.tasks["compile${variantCapitalizeName}Kotlin"].dependsOn(project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"])
//            }else{
//                project.tasks["compile${variantCapitalizeName}Java"].dependsOn(project.tasks["process${variantCapitalizeName}ToyBricksJsonFile"])
//            }
        }
    }

    /**
     * create tasks for android library
     *
     * @param project
     * @return
     */
    def createAndroidLibraryTasks() {
        project.android.libraryVariants.all { variant ->
            def variantCapitalizeName = "${variant.name}".capitalize()

            project.task("copy${variantCapitalizeName}ToyBricksJsonFile", type: Copy) {
                if (type == CONFIGURATION_TYPE_KAPT){
                    from "${project.buildDir}/generated/source/kapt/${variant.name}"
                }else {
                    from "${project.buildDir}/generated/source/apt/${variant.name}"
                }
                into "${project.buildDir}/intermediates/bundles/${variant.dirName}"
            }
            project.tasks["bundle${variantCapitalizeName}"].dependsOn(project.tasks["copy${variantCapitalizeName}ToyBricksJsonFile"])
            project.tasks["copy${variantCapitalizeName}ToyBricksJsonFile"].dependsOn(project.tasks["transformClassesAndResourcesWithSyncLibJarsFor${variantCapitalizeName}"])
            project.tasks["transformClassesAndResourcesWithSyncLibJarsFor${variantCapitalizeName}"].finalizedBy(project.tasks["copy${variantCapitalizeName}ToyBricksJsonFile"])
        }
    }
}

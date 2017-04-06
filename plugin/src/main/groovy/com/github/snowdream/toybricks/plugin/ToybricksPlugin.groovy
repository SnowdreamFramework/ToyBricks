package com.github.snowdream.toybricks.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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

    private Project project

    void apply(Project project) {
        this.project = project

        checkEnvironment()

        project.android.applicationVariants.all { variant ->
            def variantCapitalizeName = "${variant.name}".capitalize()

            def aptOutputDir = project.file("build/generated/source/apt")
            def aptOutput = new File(aptOutputDir, variant.dirName)
            project.android.sourceSets[getSourceSetName(variant)].java.srcDirs += aptOutput.getPath()

            project.task("process${variantCapitalizeName}ToyBricksJsonFile", type: ToybricksTask) {
                description "Merge ToyBricks.json from dependencies into one, Then generate java source file: InterfaceLoaderImpl.java."

                dependencies = project.configurations.compile
                isKotlinAndroidProject = hasPlugin(PLUGIN_ANDROID_KOTLIN)
                if (isKotlinAndroidProject) {
                    outputDirs = project.files(new File("${project.buildDir}", "generated/source/kapt/${variant.name}"),
                            new File("${project.buildDir}", "generated/source/apt/${variant.name}"))
                } else {
                    outputDirs = project.files(new File("${project.buildDir}", "generated/source/kapt/${variant.name}"))
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
                    source = aptOutput.getPath()
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

    def checkEnvironment() {
        if (!hasPlugin(PLUGIN_ANDROID_APP)) {
            throw new StopExecutionException(
                    "Must be applied after 'android' plugin.")
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

    private String getSourceSetName(def variant) {
        return new File(variant.dirName).getName();
    }
}

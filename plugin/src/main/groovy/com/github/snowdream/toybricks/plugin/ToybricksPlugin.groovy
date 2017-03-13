package com.github.snowdream.toybricks.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

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
            project.task("process${variantCapitalizeName}ToyBricksJsonFile", type: ToybricksTask) {
                description "Merge ToyBricks.json from dependencies into one, Then generate java source file: InterfaceLoaderImpl.java."

                dependencies = project.configurations.compile
                isKotlinAndroidProject = true //hasPlugin(PLUGIN_ANDROID_KOTLIN)
                if (isKotlinAndroidProject){
                    outputDir = new File("${project.buildDir}","generated/source/kapt/${variant.name}")
                }else{
                    outputDir = new File("${project.buildDir}","generated/source/apt/${variant.name}")
                }
            }
//            project.tasks["copy${variantCapitalizeName}ToyBricksJsonFile"].dependsOn(tasks["extract${variantCapitalizeName}Annotations"])
//            project.tasks["extract${variantCapitalizeName}Annotations"].finalizedBy(tasks["copy${variantCapitalizeName}ToyBricksJsonFile"])
        }
    }

    def checkEnvironment() {
        if (!hasPlugin(PLUGIN_ANDROID_APP)){
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
    private boolean hasPlugin(String name){
        return project.plugins.hasPlugin(name)
    }
}

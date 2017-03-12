package com.github.snowdream.toybricks.plugin

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Copy


/**
 * Created by snowdream on 17/3/8.
 * @author snowdream
 * @date 2017/03/08
 */
class ToybricksPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.android.applicationVariants.all { variant ->
            def variantCapitalizeName = "${variant.name}".capitalize()
            project.task("process${variantCapitalizeName}ToyBricksJsonFile", type: ToybricksTask) {
                description "Merge ToyBricks.json from dependencies into one, Then generate java source file: InterfaceLoaderImpl.java."
            }
//            project.tasks["copy${variantCapitalizeName}ToyBricksJsonFile"].dependsOn(tasks["extract${variantCapitalizeName}Annotations"])
//            project.tasks["extract${variantCapitalizeName}Annotations"].finalizedBy(tasks["copy${variantCapitalizeName}ToyBricksJsonFile"])
        }
    }
}

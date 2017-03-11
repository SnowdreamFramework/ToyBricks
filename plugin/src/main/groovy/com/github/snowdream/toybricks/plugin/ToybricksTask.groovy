package com.github.snowdream.toybricks.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by snowdream on 17/3/8.
 * @author snowdream
 * @date 2017/03/08
 */
class ToybricksTask extends DefaultTask {
    String greeting = 'hello from ToybricksTask'

    @TaskAction
    def greet() {
        println greeting
    }
}
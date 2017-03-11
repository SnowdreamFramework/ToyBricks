package com.github.snowdream.toybricks.plugin

import org.gradle.api.Project
import org.gradle.api.Plugin
/**
 * Created by snowdream on 17/3/8.
 * @author snowdream
 * @date 2017/03/08
 */
class ToybricksPlugin implements Plugin<Project> {
    void apply(Project target) {
        target.task('hello', type: ToybricksTask)
    }
}

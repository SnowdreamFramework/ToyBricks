package com.github.snowdream.toybricks.processor

/**
 * Created by snowdream on 17/2/12.
 */

import javax.annotation.processing.RoundEnvironment

/**
 * Description: The main base-level handler for performing some action
 */
internal interface Handler {

    /**
     * Called when the process of the [ToyBricksProcessor] is called

     * @param processorManager The manager that holds processing information
     * * *
     * *
     * @param roundEnvironment The round environment
     */
    fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment)
}
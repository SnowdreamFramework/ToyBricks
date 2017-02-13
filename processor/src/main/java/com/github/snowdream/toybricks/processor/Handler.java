package com.github.snowdream.toybricks.processor;

/**
 * Created by snowdream on 17/2/12.
 */

import javax.annotation.processing.RoundEnvironment;

/**
 * Description: The main base-level handler for performing some action
 */
interface Handler {

    /**
     * Called when the process of the [DBFlowProcessor] is called

     * @param processorManager The manager that holds processing information
     * *
     * @param roundEnvironment The round environment
     */
    void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment);
}
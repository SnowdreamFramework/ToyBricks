package com.github.snowdream.toybricks.processor

/**
 * Created by snowdream on 17/3/11.
 * @author snowdream
 * @date 2017/03/11
 */
data class ToyBricksJsonEntity(val interfaceList: MutableList<String>, val globalImplementation: MutableMap<String,String>
                               , val defaultImplementation: MutableMap<String,String>, val singletonImplementation: MutableList<String>)

package com.github.snowdream.kotlin.helloworld.impl


import com.github.snowdream.kotlin.helloworld.IKotlinText

/**
 * Created by snowdream on 17/2/11.
 */
class KotlinTextImpl : IKotlinText {
    override val text: String
    get() = "Defalt Implementation from " + javaClass.canonicalName
}

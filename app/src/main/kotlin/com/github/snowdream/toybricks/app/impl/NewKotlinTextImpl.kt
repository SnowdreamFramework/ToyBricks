package com.github.snowdream.kotlin.helloworld.impl

import com.github.snowdream.kotlin.helloworld.IKotlinText
import com.github.snowdream.toybricks.annotation.Implementation

/**
 * Created by snowdream on 17/2/11.
 */
@Implementation(IKotlinText::class)
class NewKotlinTextImpl : IKotlinText {
    override val text: String
        get() = "NewTextImpl Implementation from " + javaClass.canonicalName
}

package com.navercorp.fixturemonkey.kotlin.expression

val <T : Any> T.root: Class<T>
    get() = this.javaClass

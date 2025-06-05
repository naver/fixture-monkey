package com.navercorp.fixturemonkey.tests.kotlin.specs

class CircularReferenceValueNullable(
    val value: CircularReferenceDefaultArgument? = null
)

class CircularReferenceDefaultArgument(
    val value: CircularReferenceValueNullable = CircularReferenceValueNullable(),
)



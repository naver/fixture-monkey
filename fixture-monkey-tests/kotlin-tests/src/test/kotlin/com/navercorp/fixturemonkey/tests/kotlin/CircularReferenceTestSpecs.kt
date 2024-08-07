package com.navercorp.fixturemonkey.tests.kotlin

class CircularReferenceValueNullable(
    val value: CircularReferenceDefaultArgument? = null
)

class CircularReferenceDefaultArgument(
    val value: CircularReferenceValueNullable = CircularReferenceValueNullable(),
)



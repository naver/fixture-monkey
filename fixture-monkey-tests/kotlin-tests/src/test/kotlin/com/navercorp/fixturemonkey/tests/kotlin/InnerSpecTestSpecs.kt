package com.navercorp.fixturemonkey.tests.kotlin

import java.util.function.Supplier

class InnerSpecTestSpecs {

    data class MapObject(
        val strMap: Map<String, String>,
        val mapValueMap: Map<String, Map<String, String>>,
        val listValueMap: Map<String, List<String>>,
        val listListValueMap: Map<String, List<List<String>>>,
        val objectValueMap: Map<String, SimpleObject>,
        val objectKeyMap: Map<SimpleObject, String>
    )

    data class NestedKeyMapObject(
        val mapKeyMap: Map<Map<String, String>, String>,
    )

    data class ListStringObject(
        val values: List<String>,
    )

    data class NestedListStringObject(
        val values: List<List<String>>,
    )

    data class SupplierStringObject(
        val value: Supplier<String>
    )

    data class ComplexObjectObject(
        val value: ComplexObject,
    )

    data class ComplexObject(
        val value: SimpleObject,
    )

    data class SimpleObject(
        val str: String,
        val integer: Int
    )

    data class IntegerMapObject(
        var integerMap: Map<Int, Int>,
    )
}

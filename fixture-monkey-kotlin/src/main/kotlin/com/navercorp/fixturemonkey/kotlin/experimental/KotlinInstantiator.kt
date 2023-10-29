/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.kotlin.experimental

import com.navercorp.fixturemonkey.api.experimental.ConstructorInstantiator
import com.navercorp.fixturemonkey.api.experimental.Instantiator
import com.navercorp.fixturemonkey.api.experimental.InstantiatorProcessResult
import com.navercorp.fixturemonkey.api.experimental.InstantiatorProcessor
import com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolveParameterTypes
import com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolvedParameterNames
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames
import com.navercorp.fixturemonkey.api.property.ConstructorProperty
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types.getDeclaredConstructor
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

class KotlinInstantiatorProcessor : InstantiatorProcessor {
    override fun process(typeReference: TypeReference<*>, instantiator: Instantiator): InstantiatorProcessResult {
        return when (instantiator) {
            is ConstructorInstantiator<*> -> processConstructor(typeReference, instantiator)
            else -> throw IllegalArgumentException("Given instantiator is not valid. instantiator: ${instantiator.javaClass}")
        }
    }

    private fun processConstructor(
        typeReference: TypeReference<*>,
        instantiator: ConstructorInstantiator<*>
    ): InstantiatorProcessResult {
        val parameterTypes =
            instantiator.inputParameterTypes.map { it.type.actualType() }.toTypedArray()
        val declaredConstructor = getDeclaredConstructor(typeReference.type.actualType(), *parameterTypes)
        val kotlinConstructor = declaredConstructor.kotlinFunction!!
        val parameters: List<KParameter> = kotlinConstructor.parameters

        val inputParameterTypes = instantiator.inputParameterTypes
        val inputParameterNames = instantiator.inputParameterNames
        val parameterTypeReferences = parameters.map { it.type.javaType.toTypeReference() }
        val parameterNames = parameters.map { it.name }

        val resolveParameterTypes = resolveParameterTypes(parameterTypeReferences, inputParameterTypes)
        val resolveParameterName = resolvedParameterNames(parameterNames, inputParameterNames)

        val constructorParameterProperties = parameters.mapIndexed { index, kParameter ->
            val resolvedParameterTypeReference = resolveParameterTypes[index]
            val resolvedParameterName = resolveParameterName[index]

            ConstructorProperty(
                resolvedParameterTypeReference.annotatedType,
                declaredConstructor,
                resolvedParameterName,
                null,
                kParameter.type.isMarkedNullable,
            )
        }

        return InstantiatorProcessResult(
            ConstructorArbitraryIntrospector(
                ConstructorWithParameterNames(
                    declaredConstructor,
                    resolveParameterName
                )
            ),
            constructorParameterProperties
        )
    }
}

/**
 * The [KotlinConstructorInstantiator] class is an implementation of the [ConstructorInstantiator] interface
 * specifically designed for use in Kotlin. It allows the dynamic construction of objects of type T using a constructor
 * by specifying the parameter types and names.
 *
 * @param T The type of objects that can be instantiated using this [Instantiator].
 */
class KotlinConstructorInstantiator<T> : ConstructorInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()

    /**
     * Specifies a constructor parameter with its type inferred using reified type parameters and, optionally,
     * a parameter name. Parameters should be specified in the order they appear in the constructor's parameter list.
     *
     * @param U The type of the constructor parameter, inferred using reified type parameters.
     * @param parameterName An optional parameter name for the constructor parameter.
     * @return This [KotlinConstructorInstantiator] instance with the specified parameter added.
     */
    inline fun <reified U> parameter(parameterName: String? = null): KotlinConstructorInstantiator<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
        }

    /**
     * Get the list of types representing the input parameter types of the constructor.
     *
     * @return A list of types representing the input parameter types of the constructor.
     */
    override fun getInputParameterTypes(): List<TypeReference<*>> = _types

    /**
     * Get the list of string representing the input parameter names of the constructor.
     *
     * @return A list of string representing the input parameter names of the constructor.
     */
    override fun getInputParameterNames(): List<String?> = _parameterNames
}

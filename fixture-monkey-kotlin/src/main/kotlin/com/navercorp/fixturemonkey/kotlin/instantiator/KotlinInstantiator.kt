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

package com.navercorp.fixturemonkey.kotlin.instantiator

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.instantiator.ConstructorInstantiator
import com.navercorp.fixturemonkey.api.instantiator.FactoryMethodInstantiator
import com.navercorp.fixturemonkey.api.instantiator.Instantiator
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessor
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolveParameterTypes
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolvedParameterNames
import com.navercorp.fixturemonkey.api.instantiator.JavaBeansPropertyInstantiator
import com.navercorp.fixturemonkey.api.instantiator.JavaFieldPropertyInstantiator
import com.navercorp.fixturemonkey.api.instantiator.PropertyInstantiator
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.api.property.FieldPropertyGenerator
import com.navercorp.fixturemonkey.api.property.JavaBeansPropertyGenerator
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.api.property.TypeNameProperty
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.introspector.CompanionObjectFactoryMethodIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinPropertyArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.property.KotlinConstructorParameterProperty
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.fixturemonkey.kotlin.type.cachedMemberFunctions
import com.navercorp.fixturemonkey.kotlin.type.declaredConstructor
import com.navercorp.fixturemonkey.kotlin.type.toTypeReference
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

class KotlinInstantiatorProcessor :
    InstantiatorProcessor {
    override fun process(typeReference: TypeReference<*>, instantiator: Instantiator): InstantiatorProcessResult {
        return when (instantiator) {
            is ConstructorInstantiator<*> -> processConstructor(typeReference, instantiator)
            is FactoryMethodInstantiator<*> -> processFactoryMethod(typeReference, instantiator)
            is KotlinPropertyInstantiator<*> -> processProperty(typeReference, instantiator)
            is JavaFieldPropertyInstantiator<*> -> processJavaField(typeReference, instantiator)
            is JavaBeansPropertyInstantiator<*> -> processJavaBeansProperty(typeReference, instantiator)
            else -> throw IllegalArgumentException("Given instantiator is not valid. instantiator: ${instantiator.javaClass}")
        }
    }

    private fun processConstructor(
        typeReference: TypeReference<*>,
        instantiator: ConstructorInstantiator<*>,
    ): InstantiatorProcessResult {
        val parameterTypes =
            instantiator.inputParameterTypes.map { it.type.actualType() }.toTypedArray()
        val kotlinConstructor: KFunction<*> = typeReference.type.actualType().declaredConstructor(*parameterTypes)
        val parameters: List<KParameter> = kotlinConstructor.parameters

        val inputParameterTypes = instantiator.inputParameterTypes
        val inputParameterNames = instantiator.inputParameterNames
        val parameterTypeReferences = parameters.map { it.type.javaType.toTypeReference() }
        val parameterNames = parameters.map { it.name }

        val resolveParameterTypes = resolveParameterTypes(parameterTypeReferences, inputParameterTypes)
        val resolveParameterName = resolvedParameterNames(parameterNames, inputParameterNames)
        val useDefaultArguments = if (instantiator is KotlinConstructorInstantiator) {
            instantiator.useDefaultArguments
        } else {
            listOf()
        }
        val constructorParameterProperties = parameters
            .mapIndexedNotNull { index, kParameter ->
                val resolvedParameterTypeReference = resolveParameterTypes[index]
                val resolvedParameterName = resolveParameterName[index]
                val useDefaultArgument = useDefaultArguments.getOrElse(index) { false }
                if (kParameter.isOptional && useDefaultArgument) {
                    return@mapIndexedNotNull null
                }

                KotlinConstructorParameterProperty(
                    resolvedParameterTypeReference.annotatedType,
                    kParameter,
                    resolvedParameterName,
                    kotlinConstructor,
                )
            }

        val constructorArbitraryIntrospector = KotlinConstructorArbitraryIntrospector(kotlinConstructor)

        val propertyInstantiator = instantiator.propertyInstantiator
        if (propertyInstantiator != null) {
            val propertyInstantiatorProcessResult = this.process(typeReference, propertyInstantiator)
            return InstantiatorProcessResult(
                CompositeArbitraryIntrospector(
                    listOf(
                        constructorArbitraryIntrospector,
                        propertyInstantiatorProcessResult.introspector,
                    ),
                ),
                constructorParameterProperties + propertyInstantiatorProcessResult.properties,
            )
        }

        return InstantiatorProcessResult(
            constructorArbitraryIntrospector,
            constructorParameterProperties,
        )
    }

    private fun processProperty(
        typeReference: TypeReference<*>,
        instantiator: KotlinPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val propertyFilter = instantiator.propertyFilter
        val properties = KotlinPropertyGenerator(
            javaDelegatePropertyGenerator = { listOf() },
            propertyFilter = propertyFilter,
        ).generateChildProperties(property)

        return InstantiatorProcessResult(
            KotlinPropertyArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun processFactoryMethod(
        typeReference: TypeReference<*>,
        instantiator: FactoryMethodInstantiator<*>,
    ): InstantiatorProcessResult {
        val type = typeReference.type.actualType()
        val factoryMethodName = instantiator.factoryMethodName
        val inputParameterTypes = instantiator.inputParameterTypes.map { it.type.actualType() }
            .toTypedArray()
        val kotlinType = type.cachedKotlin()
        val companionMethod = kotlinType.companionObject?.cachedMemberFunctions()
            ?.findDeclaredMemberFunction(factoryMethodName, inputParameterTypes)
            ?: throw IllegalArgumentException("Given type $kotlinType has no static factory method.")

        val companionMethodParameters = companionMethod.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
        val methodParameterTypeReferences = companionMethodParameters.map { it.type.toTypeReference() }
        val methodParameterNames = companionMethodParameters.map { it.name }
        val inputParameterTypesReferences = instantiator.inputParameterTypes
        val inputParameterNames = instantiator.inputParameterNames

        val resolvedParameterTypes =
            resolveParameterTypes(methodParameterTypeReferences, inputParameterTypesReferences)
        val resolvedParameterNames = resolvedParameterNames(methodParameterNames, inputParameterNames)

        val properties = companionMethod.toParameterProperty(
            resolvedParameterTypes = resolvedParameterTypes,
            resolvedParameterNames = resolvedParameterNames,
        )
        return InstantiatorProcessResult(
            CompanionObjectFactoryMethodIntrospector(companionMethod),
            properties,
        )
    }

    private fun processJavaField(
        typeReference: TypeReference<*>,
        instantiator: JavaFieldPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val filterPredicate = instantiator.fieldPredicate
        val properties = FieldPropertyGenerator(filterPredicate) { true }
            .generateChildProperties(property)

        return InstantiatorProcessResult(
            FieldReflectionArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun processJavaBeansProperty(
        typeReference: TypeReference<*>,
        instantiator: JavaBeansPropertyInstantiator<*>,
    ): InstantiatorProcessResult {
        val property = PropertyUtils.toProperty(typeReference)
        val propertyDescriptorPredicate = instantiator.propertyDescriptorPredicate
        val properties = JavaBeansPropertyGenerator(propertyDescriptorPredicate) { true }
            .generateChildProperties(property)

        return InstantiatorProcessResult(
            BeanArbitraryIntrospector.INSTANCE,
            properties,
        )
    }

    private fun KFunction<*>.toParameterProperty(
        resolvedParameterTypes: List<TypeReference<*>>,
        resolvedParameterNames: List<String>,
    ): List<Property> = this.parameters
        .filter { parameter -> parameter.kind != KParameter.Kind.INSTANCE }
        .mapIndexed { index, kParameter ->
            TypeNameProperty(
                resolvedParameterTypes[index].annotatedType,
                resolvedParameterNames[index],
                kParameter.type.isMarkedNullable,
            )
        }

    private fun Collection<KFunction<*>>.findDeclaredMemberFunction(
        factoryMethodName: String,
        inputParameterTypes: Array<Class<*>>,
    ): KFunction<*>? =
        this.find { function ->
            function.name == factoryMethodName && hasAnyParameterMatchingFunction(function, inputParameterTypes)
        }

    private fun hasAnyParameterMatchingFunction(function: KFunction<*>, inputParameterTypes: Array<Class<*>>): Boolean =
        function.parameters
            .filter { parameter -> parameter.kind != KParameter.Kind.INSTANCE }
            .map { parameter -> parameter.type.javaType.actualType() }
            .let {
                inputParameterTypes.isEmpty() || Types.isAssignableTypes(it.toTypedArray(), inputParameterTypes)
            }

    internal class KotlinConstructorArbitraryIntrospector(private val kotlinConstructor: KFunction<*>) :
        ArbitraryIntrospector {
        override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult =
            ArbitraryIntrospectorResult(
                CombinableArbitrary.objectBuilder()
                    .properties(context.combinableArbitrariesByArbitraryProperty)
                    .build {
                        val valuesByParameter: Map<KParameter, Any?> =
                            it.filterKeys { key -> key.objectProperty.property is KotlinConstructorParameterProperty }
                                .mapKeys { entry -> (entry.key.objectProperty.property as KotlinConstructorParameterProperty).kParameter }

                        kotlinConstructor.isAccessible = true
                        kotlinConstructor.callBy(valuesByParameter)
                    },
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
class KotlinConstructorInstantiator<T> :
    ConstructorInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()
    val useDefaultArguments: MutableList<Boolean> = ArrayList()
    private var _propertyInstantiator: PropertyInstantiator<T>? = null

    /**
     * Specifies a constructor parameter with its type inferred using reified type parameters and, optionally,
     * a parameter name. Parameters should be specified in the order they appear in the constructor's parameter list.
     *
     * @param U The type of the constructor parameter, inferred using reified type parameters.
     * @param parameterName An optional parameter name for the constructor parameter.
     * @return This [KotlinConstructorInstantiator] instance with the specified parameter added.
     */
    inline fun <reified U> parameter(
        parameterName: String? = null,
        useDefaultArgument: Boolean = false,
    ): KotlinConstructorInstantiator<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
            useDefaultArguments.add(useDefaultArgument)
        }

    fun property(): KotlinConstructorInstantiator<T> = this.apply {
        _propertyInstantiator = KotlinPropertyInstantiator()
    }

    fun property(dsl: KotlinPropertyInstantiator<T>.() -> KotlinPropertyInstantiator<T>): KotlinConstructorInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(KotlinPropertyInstantiator())
        }

    fun javaField(): KotlinConstructorInstantiator<T> = this.apply {
        _propertyInstantiator =
            JavaFieldPropertyInstantiator<T>()
    }

    fun javaField(dsl: JavaFieldPropertyInstantiator<T>.() -> JavaFieldPropertyInstantiator<T>): KotlinConstructorInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(JavaFieldPropertyInstantiator())
        }

    fun javaBeansProperty(): KotlinConstructorInstantiator<T> = this.apply {
        _propertyInstantiator =
            JavaBeansPropertyInstantiator<T>()
    }

    fun javaBeansProperty(dsl: JavaBeansPropertyInstantiator<T>.() -> JavaBeansPropertyInstantiator<T>): KotlinConstructorInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(JavaBeansPropertyInstantiator())
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
    override fun getPropertyInstantiator(): PropertyInstantiator<T>? = _propertyInstantiator
}

class KotlinFactoryMethodInstantiator<T>(private val _factoryMethodName: String) :
    FactoryMethodInstantiator<T> {
    val _types: MutableList<TypeReference<*>> = ArrayList()
    val _parameterNames: MutableList<String?> = ArrayList()
    private var _propertyInstantiator: PropertyInstantiator<T>? = null

    inline fun <reified U> parameter(parameterName: String? = null): KotlinFactoryMethodInstantiator<T> =
        this.apply {
            _types.add(object : TypeReference<U>() {})
            _parameterNames.add(parameterName)
        }

    fun property(): KotlinFactoryMethodInstantiator<T> = this.apply {
        _propertyInstantiator = KotlinPropertyInstantiator()
    }

    fun property(dsl: KotlinPropertyInstantiator<T>.() -> KotlinPropertyInstantiator<T>): KotlinFactoryMethodInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(KotlinPropertyInstantiator())
        }

    fun javaField(): KotlinFactoryMethodInstantiator<T> = this.apply {
        _propertyInstantiator =
            JavaFieldPropertyInstantiator<T>()
    }

    fun javaField(dsl: JavaFieldPropertyInstantiator<T>.() -> JavaFieldPropertyInstantiator<T>): KotlinFactoryMethodInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(JavaFieldPropertyInstantiator())
        }

    fun javaBeansProperty(): KotlinFactoryMethodInstantiator<T> = this.apply {
        _propertyInstantiator =
            JavaBeansPropertyInstantiator<T>()
    }

    fun javaBeansProperty(dsl: JavaBeansPropertyInstantiator<T>.() -> JavaBeansPropertyInstantiator<T>): KotlinFactoryMethodInstantiator<T> =
        this.apply {
            _propertyInstantiator = dsl(JavaBeansPropertyInstantiator())
        }

    override fun getFactoryMethodName(): String = _factoryMethodName
    override fun getInputParameterTypes(): List<TypeReference<*>> = _types
    override fun getInputParameterNames(): List<String?> = _parameterNames
    override fun getPropertyInstantiator(): PropertyInstantiator<T>? = _propertyInstantiator
}

class KotlinPropertyInstantiator<T>(
    internal var propertyFilter: (property: KProperty<*>) -> Boolean = { true },
) : PropertyInstantiator<T> {
    fun filter(propertyFilter: (KProperty<*>) -> Boolean): KotlinPropertyInstantiator<T> {
        this.propertyFilter = propertyFilter
        return this
    }
}

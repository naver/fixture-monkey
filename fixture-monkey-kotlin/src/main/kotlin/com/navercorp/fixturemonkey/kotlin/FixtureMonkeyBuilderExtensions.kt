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

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.api.property.PropertyGenerator
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver
import kotlin.reflect.KClass

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypePropertyGenerator(propertyGenerator: PropertyGenerator) =
    this.pushAssignableTypePropertyGenerator(
        T::class.java,
        propertyGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypePropertyGenerator(propertyGenerator: PropertyGenerator) =
    this.pushExactTypePropertyGenerator(
        T::class.java,
        propertyGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypeObjectPropertyGenerator(objectPropertyGenerator: ObjectPropertyGenerator) =
    this.pushAssignableTypeObjectPropertyGenerator(
        T::class.java,
        objectPropertyGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypeObjectPropertyGenerator(objectPropertyGenerator: ObjectPropertyGenerator) =
    this.pushExactTypeObjectPropertyGenerator(
        T::class.java,
        objectPropertyGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypeContainerPropertyGenerator(
    containerPropertyGenerator: ContainerPropertyGenerator
) = this.pushAssignableTypeContainerPropertyGenerator(
    T::class.java,
    containerPropertyGenerator
)

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypeContainerPropertyGenerator(containerPropertyGenerator: ContainerPropertyGenerator) =
    this.pushExactTypeContainerPropertyGenerator(
        T::class.java,
        containerPropertyGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypePropertyNameResolver(propertyNameResolver: PropertyNameResolver) =
    this.pushAssignableTypePropertyNameResolver(
        T::class.java,
        propertyNameResolver
    )

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypePropertyNameResolver(propertyNameResolver: PropertyNameResolver) =
    this.pushExactTypePropertyNameResolver(
        T::class.java,
        propertyNameResolver
    )

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypeNullInjectGenerator(nullInjectGenerator: NullInjectGenerator) =
    this.pushExactTypeNullInjectGenerator(
        T::class.java,
        nullInjectGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypeNullInjectGenerator(nullInjectGenerator: NullInjectGenerator) =
    this.pushAssignableTypeNullInjectGenerator(
        T::class.java,
        nullInjectGenerator
    )

inline fun <reified T> FixtureMonkeyBuilder.pushAssignableTypeArbitraryIntrospector(arbitraryIntrospector: ArbitraryIntrospector) =
    this.pushAssignableTypeArbitraryIntrospector(
        T::class.java,
        arbitraryIntrospector
    )

inline fun <reified T> FixtureMonkeyBuilder.pushExactTypeArbitraryIntrospector(arbitraryIntrospector: ArbitraryIntrospector) =
    this.pushExactTypeArbitraryIntrospector(
        T::class.java,
        arbitraryIntrospector
    )

inline fun <reified T> FixtureMonkeyBuilder.addExceptGenerateClass(): FixtureMonkeyBuilder =
    this.addExceptGenerateClass(T::class.java)

fun FixtureMonkeyBuilder.addExceptGenerateClasses(
    vararg kClasses: KClass<*>
): FixtureMonkeyBuilder =
    this.addExceptGenerateClasses(*(kClasses.map { it.java }.toTypedArray()))

inline fun <reified T> FixtureMonkeyBuilder.register(
    noinline arbitraryBuilderGenerator: (fixtureMonkey: FixtureMonkey) -> ArbitraryBuilder<out T>
): FixtureMonkeyBuilder = this.register(T::class.java, arbitraryBuilderGenerator)

inline fun <reified T> FixtureMonkeyBuilder.registerExactType(
    noinline arbitraryBuilderGenerator: (fixtureMonkey: FixtureMonkey) -> ArbitraryBuilder<out T>
): FixtureMonkeyBuilder = this.registerExactType(T::class.java, arbitraryBuilderGenerator)

inline fun <reified T> FixtureMonkeyBuilder.registerAssignableType(
    noinline arbitraryBuilderGenerator: (fixtureMonkey: FixtureMonkey) -> ArbitraryBuilder<out T>
): FixtureMonkeyBuilder = this.registerAssignableType(T::class.java, arbitraryBuilderGenerator)

fun FixtureMonkeyBuilder.registerGroup(
    vararg kClasses: KClass<*>
): FixtureMonkeyBuilder =
    this.registerGroup(*(kClasses.map { it.java }.toTypedArray()))

inline fun <reified T> FixtureMonkeyBuilder.addContainerType(
    containerObjectPropertyGenerator: ContainerPropertyGenerator,
    containerArbitraryIntrospector: ArbitraryIntrospector,
    decomposedContainerValueFactory: DecomposedContainerValueFactory
): FixtureMonkeyBuilder = this.addContainerType(
    T::class.java,
    containerObjectPropertyGenerator,
    containerArbitraryIntrospector,
    decomposedContainerValueFactory
)

inline fun <reified T : Any> InterfacePlugin.interfaceImplements(
    matcher: Matcher,
    implementations: List<KClass<out T>>
): InterfacePlugin =
    this.interfaceImplements(matcher, implementations.map { it.java })

inline fun <reified T : Any> InterfacePlugin.interfaceImplements(
    vararg implementations: KClass<out T>
): InterfacePlugin = this.interfaceImplements(T::class.java, implementations.map { it.java })

inline fun <reified T : Any> InterfacePlugin.interfaceImplements(
    implementations: List<KClass<out T>>
): InterfacePlugin = this.interfaceImplements(T::class.java, implementations.map { it.java })

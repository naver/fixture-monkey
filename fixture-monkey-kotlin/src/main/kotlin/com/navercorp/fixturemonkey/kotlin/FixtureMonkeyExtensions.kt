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
import com.navercorp.fixturemonkey.api.ObjectBuilder
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.expression.TypedPropertySelector
import com.navercorp.fixturemonkey.api.instantiator.Instantiator
import com.navercorp.fixturemonkey.api.property.PropertySelector
import com.navercorp.fixturemonkey.api.type.TypeReference
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContextProvider
import com.navercorp.fixturemonkey.customizer.InnerSpec
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder
import net.jqwik.api.Arbitrary
import net.jqwik.api.Combinators
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1
import kotlin.streams.asSequence

inline fun <reified T : Any?> FixtureMonkey.giveMe(): Sequence<T> =
    this.giveMe(object : TypeReference<T>() {}).asSequence()

inline fun <reified T : Any?> FixtureMonkey.giveMe(size: Int): List<T> =
    this.giveMe(object : TypeReference<T>() {}, size)

inline fun <reified T : Any?> FixtureMonkey.giveMeOne(): T = this.giveMeOne(object : TypeReference<T>() {})

inline fun <reified T : Any?> FixtureMonkey.giveMeArbitrary(): Arbitrary<T> =
    this.giveMeArbitrary(object : TypeReference<T>() {})

inline fun <reified T : Any?> FixtureMonkey.giveMeBuilder(): ArbitraryBuilder<T> =
    this.giveMeBuilder(object : TypeReference<T>() {})

inline fun <reified T : Any?> FixtureMonkey.giveMeKotlinBuilder(): KotlinTypeDefaultArbitraryBuilder<T> =
    InternalKotlinTypeDefaultArbitraryBuilder(this.giveMeBuilder(object : TypeReference<T>() {}))

inline fun <reified T : Any?> FixtureMonkey.giveMeKotlinBuilder(value: T): KotlinTypeDefaultArbitraryBuilder<T> =
    InternalKotlinTypeDefaultArbitraryBuilder(this.giveMeBuilder(value))

inline fun <reified T : Any?> FixtureMonkey.giveMeExperimentalBuilder(): ExperimentalArbitraryBuilder<T> =
    this.giveMeExperimentalBuilder(object : TypeReference<T>() {})

open class KotlinTypeDefaultArbitraryBuilder<T> internal constructor(val delegate: ArbitraryBuilder<T>) :
    ArbitraryBuilder<T> {
    override fun set(expression: String, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> = this.apply {
        delegate.set(expression, value)
    }

    override fun set(expression: String, value: Any?, limit: Int): KotlinTypeDefaultArbitraryBuilder<T> = this.apply {
        delegate.set(expression, value, limit)
    }

    override fun set(propertySelector: PropertySelector, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply {
            delegate.set(propertySelector, value)
        }

    override fun set(
        propertySelector: PropertySelector,
        value: Any?,
        limit: Int,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.set(propertySelector, value, limit) }

    override fun set(value: Any?): KotlinTypeDefaultArbitraryBuilder<T> = this.apply {
        delegate.set(value)
    }

    override fun setInner(innerSpec: InnerSpec): KotlinTypeDefaultArbitraryBuilder<T> = this.apply {
        delegate.setInner(innerSpec)
    }

    override fun setLazy(expression: String, supplier: Supplier<*>?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply {
            delegate.setLazy(expression, supplier)
        }

    override fun setLazy(expression: String, supplier: Supplier<*>?, limit: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setLazy(expression, supplier, limit) }

    override fun setLazy(
        propertySelector: PropertySelector,
        supplier: Supplier<*>?,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setLazy(propertySelector, supplier) }

    override fun setLazy(
        propertySelector: PropertySelector,
        supplier: Supplier<*>?,
        limit: Int,
    ): KotlinTypeDefaultArbitraryBuilder<T> = this.apply { delegate.setLazy(propertySelector, supplier, limit) }

    override fun setNull(expression: String): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setNull(expression) }

    override fun setNull(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setNull(propertySelector) }

    override fun setNotNull(expression: String): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setNotNull(expression) }

    override fun setNotNull(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setNotNull(propertySelector) }

    override fun <U> setPostCondition(
        expression: String,
        type: Class<U>,
        predicate: Predicate<U>,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setPostCondition(expression, type, predicate) }

    override fun <U> setPostCondition(
        propertySelector: PropertySelector,
        type: Class<U>,
        predicate: Predicate<U>,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setPostCondition(propertySelector, type, predicate) }

    override fun <U> setPostCondition(
        expression: String,
        type: Class<U>,
        predicate: Predicate<U>,
        limit: Int,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setPostCondition(expression, type, predicate, limit) }

    override fun <U> setPostCondition(
        propertySelector: PropertySelector,
        type: Class<U>,
        predicate: Predicate<U>,
        limit: Int,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setPostCondition(propertySelector, type, predicate, limit) }

    override fun setPostCondition(predicate: Predicate<T>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setPostCondition(predicate) }

    override fun size(expression: String, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.size(expression, size) }

    override fun size(propertySelector: PropertySelector, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.size(propertySelector, size) }

    override fun size(expression: String, minSize: Int, maxSize: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.size(expression, minSize, maxSize) }

    override fun size(
        propertySelector: PropertySelector,
        minSize: Int,
        maxSize: Int,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.size(propertySelector, minSize, maxSize) }

    override fun minSize(expression: String, minSize: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.minSize(expression, minSize) }

    override fun minSize(propertySelector: PropertySelector, minSize: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.minSize(propertySelector, minSize) }

    override fun maxSize(expression: String, maxSize: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.maxSize(expression, maxSize) }

    override fun maxSize(propertySelector: PropertySelector, maxSize: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.maxSize(propertySelector, maxSize) }

    override fun fixed(): KotlinTypeDefaultArbitraryBuilder<T> = this.apply { delegate.fixed() }

    override fun <R> zipWith(
        others: List<ArbitraryBuilder<*>>,
        combinator: Function<List<*>, R>,
    ): KotlinTypeDefaultArbitraryBuilder<R> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.zipWith(others, combinator))

    override fun <U, R> zipWith(
        other: ArbitraryBuilder<U>,
        combinator: BiFunction<T, U, R>,
    ): KotlinTypeDefaultArbitraryBuilder<R> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.zipWith(other, combinator))

    override fun <U, V, R> zipWith(
        other: ArbitraryBuilder<U>,
        another: ArbitraryBuilder<V>,
        combinator: Combinators.F3<T, U, V, R>,
    ): KotlinTypeDefaultArbitraryBuilder<R> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.zipWith(other, another, combinator))

    override fun <U, V, W, R> zipWith(
        other: ArbitraryBuilder<U>,
        another: ArbitraryBuilder<V>,
        theOther: ArbitraryBuilder<W>,
        combinator: Combinators.F4<T, U, V, W, R>,
    ): KotlinTypeDefaultArbitraryBuilder<R> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.zipWith(other, another, theOther, combinator))

    override fun sampleList(size: Int): List<T> = delegate.sampleList(size)

    override fun validOnly(validOnly: Boolean): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.validOnly(validOnly) }

    override fun instantiate(instantiator: Instantiator): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.instantiate(instantiator) }

    override fun instantiate(type: Class<*>, instantiator: Instantiator): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.instantiate(type, instantiator) }

    override fun instantiate(
        type: TypeReference<*>,
        instantiator: Instantiator,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.instantiate(type, instantiator) }

    override fun <U : Any?> customizeProperty(
        propertySelector: TypedPropertySelector<U>,
        combinableArbitraryCustomizer: Function<CombinableArbitrary<out U>, CombinableArbitrary<out U>>
    ): ArbitraryBuilder<T> = this.apply { delegate.customizeProperty(propertySelector, combinableArbitraryCustomizer) }

    override fun build(): Arbitrary<T> = delegate.build()

    override fun sample(): T = delegate.sample()

    override fun sampleStream(): Stream<T> = delegate.sampleStream()

    override fun copy(): KotlinTypeDefaultArbitraryBuilder<T> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.copy())

    override fun thenApply(biConsumer: BiConsumer<T, ArbitraryBuilder<T>>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.thenApply(biConsumer) }

    override fun acceptIf(
        predicate: Predicate<T>,
        consumer: Consumer<ArbitraryBuilder<T>>,
    ): KotlinTypeDefaultArbitraryBuilder<T> = this.apply { delegate.acceptIf(predicate, consumer) }

    override fun <U> map(mapper: Function<T, U>): KotlinTypeDefaultArbitraryBuilder<U> =
        InternalKotlinTypeDefaultArbitraryBuilder(delegate.map(mapper))

    fun setInner(innerSpecConfigurer: (InnerSpec.() -> InnerSpec)): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.setInner(innerSpecConfigurer(InnerSpec())) }

    fun setKotlinInner(configure: KotlinInnerSpec.() -> Unit): KotlinTypeDefaultArbitraryBuilder<T> {
        val kotlinSpec = KotlinInnerSpec()
        kotlinSpec.configure()
        return this.setInner(kotlinSpec.toInnerSpec())
    }

    /**
     * The following are the APIs use the [PropertySelector].
     */
    fun set(property: KProperty1<T, Any?>, value: Any?, limit: Long): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertyExpressionGenerator(property), value, limit.toInt())

    fun set(property: KProperty1<T, Any?>, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertyExpressionGenerator(property), value)

    @JvmName("setRoot")
    fun set(property: KProperty1<T, Class<T>>, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(value)

    fun setExp(property: KProperty1<T, Any?>, value: Any?, limit: Long): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertyExpressionGenerator(property), value, limit.toInt())

    fun setExp(property: KProperty1<T, Any?>, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertyExpressionGenerator(property), value)

    fun setExp(
        propertySelector: PropertySelector,
        value: Any?,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertySelector, value, limit.toInt())

    fun setExp(propertySelector: PropertySelector, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertySelector, value)

    fun setExpGetter(property: KFunction1<T, Any?>, value: Any?, limit: Long): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(
            propertyExpressionGenerator(property),
            value,
            limit.toInt(),
        )

    fun setExpGetter(property: KFunction1<T, Any?>, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertyExpressionGenerator(property), value)

    fun setExpGetter(
        propertySelector: PropertySelector,
        value: Any?,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertySelector, value, limit.toInt())

    fun setExpGetter(propertySelector: PropertySelector, value: Any?): KotlinTypeDefaultArbitraryBuilder<T> =
        this.set(propertySelector, value)

    inline fun <reified U> setPostCondition(
        expression: String,
        noinline filter: (U) -> Boolean,
        limit: Int,
    ) = this.apply { delegate.setPostCondition(expression, U::class.java, filter, limit) }

    inline fun <reified U> setPostCondition(
        expression: String,
        noinline filter: (U) -> Boolean,
    ) = this.apply { delegate.setPostCondition(expression, U::class.java, filter) }

    inline fun <reified U> setPostCondition(
        property: KProperty1<T, *>,
        noinline filter: (U) -> Boolean,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
            limit.toInt(),
        )

    @JvmName("setPostConditionRoot")
    fun setPostCondition(
        property: KProperty1<T, Class<T>>,
        filter: (T) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(filter)

    inline fun <reified U> setPostCondition(
        property: KProperty1<T, *>,
        noinline filter: (U) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
        )

    inline fun <reified U> setPostCondition(
        propertySelector: PropertySelector,
        noinline predicate: (U) -> Boolean,
        limit: Int,
    ) = this.apply { delegate.setPostCondition(propertySelector, U::class.java, predicate, limit) }

    inline fun <reified U> setPostCondition(
        propertySelector: PropertySelector,
        noinline predicate: (U) -> Boolean,
    ) = this.apply { delegate.setPostCondition(propertySelector, U::class.java, predicate) }

    inline fun <reified U> setPostConditionExp(
        property: KProperty1<T, *>,
        noinline filter: (U) -> Boolean,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
            limit.toInt(),
        )

    inline fun <reified U> setPostConditionExp(
        property: KProperty1<T, *>,
        noinline filter: (U) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
        )

    inline fun <reified U> setPostConditionExp(
        propertySelector: PropertySelector,
        noinline filter: (U) -> Boolean,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertySelector,
            U::class.java,
            filter,
            limit.toInt(),
        )

    inline fun <reified U> setPostConditionExp(
        propertySelector: PropertySelector,
        noinline filter: (U) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertySelector,
            U::class.java,
            filter,
        )

    inline fun <reified U> setPostConditionExpGetter(
        propertySelector: PropertySelector,
        noinline filter: (U) -> Boolean,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertySelector,
            U::class.java,
            filter,
            limit.toInt(),
        )

    inline fun <reified U> setPostConditionExpGetter(
        propertySelector: PropertySelector,
        noinline filter: (U) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertySelector,
            U::class.java,
            filter,
        )

    inline fun <reified U> setPostConditionExpGetter(
        property: KFunction1<T, *>,
        noinline filter: (U) -> Boolean,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
            limit.toInt(),
        )

    inline fun <reified U> setPostConditionExpGetter(
        property: KFunction1<T, *>,
        noinline filter: (U) -> Boolean,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setPostCondition(
            propertyExpressionGenerator(property),
            U::class.java,
            filter,
        )

    fun setNull(property: KProperty1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNull(propertyExpressionGenerator(property))

    fun setNullExp(property: KProperty1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNull(propertyExpressionGenerator(property))

    fun setNullExp(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNull(propertySelector)

    fun setNullExpGetter(property: KFunction1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNull(propertyExpressionGenerator(property))

    fun setNullExpGetter(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNull(propertySelector)

    fun setNotNull(property: KProperty1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNotNull(propertyExpressionGenerator(property))

    fun setNotNullExp(property: KProperty1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNotNull(propertyExpressionGenerator(property))

    fun setNotNullExp(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNotNull(propertySelector)

    fun setNotNullExpGetter(property: KFunction1<T, *>): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNotNull(propertyExpressionGenerator(property))

    fun setNotNullExpGetter(propertySelector: PropertySelector): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setNotNull(propertySelector)

    @JvmName("sizeRoot")
    fun size(property: KProperty1<T, Class<T>>, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size("$", size)

    fun size(property: KProperty1<T, *>, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), size)

    @JvmName("sizeRoot")
    fun size(property: KProperty1<T, Class<T>>, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size("$", min, max)

    fun size(property: KProperty1<T, *>, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), min, max)

    fun sizeExp(property: KProperty1<T, *>, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), size)

    fun sizeExp(propertySelector: PropertySelector, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertySelector, size)

    fun sizeExp(propertySelector: PropertySelector, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertySelector, min, max)

    fun sizeExp(property: KProperty1<T, *>, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), min, max)

    fun sizeExpGetter(property: KFunction1<T, *>, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), size)

    fun sizeExpGetter(property: KFunction1<T, *>, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertyExpressionGenerator(property), min, max)

    fun sizeExpGetter(propertySelector: PropertySelector, size: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertySelector, size)

    fun sizeExpGetter(propertySelector: PropertySelector, min: Int, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.size(propertySelector, min, max)

    @JvmName("minSizeRoot")
    fun minSize(property: KProperty1<T, Class<T>>, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize("$", min)

    fun minSize(property: KProperty1<T, *>, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize(propertyExpressionGenerator(property), min)

    fun minSizeExp(property: KProperty1<T, *>, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize(propertyExpressionGenerator(property), min)

    fun minSizeExp(propertySelector: PropertySelector, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize(propertySelector, min)

    fun minSizeExpGetter(property: KFunction1<T, *>, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize(propertyExpressionGenerator(property), min)

    fun minSizeExpGetter(propertySelector: PropertySelector, min: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.minSize(propertySelector, min)

    @JvmName("maxSizeRoot")
    fun maxSize(property: KProperty1<T, Class<T>>, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize("$", max)

    fun maxSize(property: KProperty1<T, *>, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize(propertyExpressionGenerator(property), max)

    fun maxSizeExp(property: KProperty1<T, *>, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize(propertyExpressionGenerator(property), max)

    fun maxSizeExp(propertySelector: PropertySelector, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize(propertySelector, max)

    fun maxSizeExpGetter(property: KFunction1<T, *>, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize(propertyExpressionGenerator(property), max)

    fun maxSizeExpGetter(propertySelector: PropertySelector, max: Int): KotlinTypeDefaultArbitraryBuilder<T> =
        this.maxSize(propertySelector, max)

    fun setLazy(
        property: KProperty1<T, Any?>,
        supplier: Supplier<Any?>,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

    @JvmName("setLazyRoot")
    fun setLazy(
        property: KProperty1<T, Class<T>>,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> = this.setLazy("$", supplier)

    fun setLazy(
        property: KProperty1<T, Any?>,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier)

    fun setLazyExp(
        property: KProperty1<T, Any?>,
        supplier: Supplier<Any?>,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

    fun setLazyExp(
        property: KProperty1<T, Any?>,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> = this.setLazy(propertyExpressionGenerator(property), supplier)

    fun setLazyExp(
        propertySelector: PropertySelector,
        supplier: Supplier<Any?>,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertySelector, supplier, limit.toInt())

    fun setLazyExp(
        propertySelector: PropertySelector,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertySelector, supplier)

    fun setLazyExpGetter(
        property: KFunction1<T, Any?>,
        supplier: Supplier<Any?>,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertyExpressionGenerator(property), supplier, limit.toInt())

    fun setLazyExpGetter(
        property: KFunction1<T, Any?>,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertyExpressionGenerator(property), supplier)

    fun setLazyExpGetter(
        propertySelector: PropertySelector,
        supplier: Supplier<Any?>,
        limit: Long,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertySelector, supplier, limit.toInt())

    fun setLazyExpGetter(
        propertySelector: PropertySelector,
        supplier: Supplier<Any?>,
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.setLazy(propertySelector, supplier)

    fun <U> customizeProperty(
        property: KProperty1<T, U?>,
        combinableArbitraryCustomizer: Function<CombinableArbitrary<out U>, CombinableArbitrary<out U>>
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.customizeProperty(propertyExpressionGenerator(property), combinableArbitraryCustomizer) }

    fun <U> customizeProperty(
        property: KFunction1<T, U?>,
        combinableArbitraryCustomizer: Function<CombinableArbitrary<out U>, CombinableArbitrary<out U>>
    ): KotlinTypeDefaultArbitraryBuilder<T> =
        this.apply { delegate.customizeProperty(propertyExpressionGenerator(property), combinableArbitraryCustomizer) }
}

class InternalKotlinTypeDefaultArbitraryBuilder<T>(delegate: ArbitraryBuilder<T>) :
    KotlinTypeDefaultArbitraryBuilder<T>(delegate), ArbitraryBuilderContextProvider, ObjectBuilder<T> {
    override fun getActiveContext(): ArbitraryBuilderContext =
        (delegate as ArbitraryBuilderContextProvider).activeContext
}

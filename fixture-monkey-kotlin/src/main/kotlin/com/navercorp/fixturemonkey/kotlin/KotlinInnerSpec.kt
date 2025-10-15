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

import com.navercorp.fixturemonkey.customizer.InnerSpec
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import org.jspecify.annotations.Nullable
import java.util.function.Predicate

/**
 * A Kotlin DSL wrapper for {@link InnerSpec} that provides type-safe configuration of nested properties.
 * <p>
 * Provides methods for setting configuration for nested properties in a Kotlin-idiomatic way,
 * and can be particularly useful for configuring map-type properties with DSL syntax.
 * </p>
 * <p>
 * Instances of this class can be reused to consistently and easily configure nested properties.
 * </p>
 *
 * @since 1.1.12
 */
@Suppress("UnusedReturnValue", "TooManyFunctions")
@API(since = "1.1.12", status = Status.EXPERIMENTAL)
class KotlinInnerSpec {

    private val operations = mutableListOf<(InnerSpec) -> Unit>()

    /**
     * Configures the currently referred property with the specifications provided inside the given {@link InnerSpec} object
     *
     * @param innerSpec An instance of {@link InnerSpec} containing the specifications
     *                  to be applied to the currently referred property.
     */
    fun inner(innerSpec: InnerSpec): KotlinInnerSpec {
        return applyToSpec { it.inner(innerSpec) }
    }

    /**
     * Sets the size of the currently referred container property.
     * {@code minSize} should be less than or equal to {@code maxSize}.
     *
     * @param minSize minimum size of the container to generate
     * @param maxSize maximum size of the container to generate
     */
    fun size(minSize: Int, maxSize: Int): KotlinInnerSpec {
        return applyToSpec { it.size(minSize, maxSize) }
    }

    /**
     * Sets the size of the currently referred container property.
     *
     * @param size size of the container to generate
     */
    fun size(size: Int): KotlinInnerSpec {
        return applyToSpec { it.size(size) }
    }

    /**
     * Sets the size of the currently referred container property.
     * The size of container property would be between {@code minSize} and
     * {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}
     *
     * @param minSize minimum size of the container to generate
     */
    fun minSize(minSize: Int): KotlinInnerSpec {
        return applyToSpec { it.minSize(minSize) }
    }

    /**
     * Sets the size of the currently referred container property.
     * The size of container property would be between
     * max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}) and {@code maxSize}
     *
     * @param maxSize maximum size of the container to generate
     */
    fun maxSize(maxSize: Int): KotlinInnerSpec {
        return applyToSpec { it.maxSize(maxSize) }
    }

    /**
     * Sets a key in the currently referred map property.
     *
     * @param key value of the map key to set
     */
    fun key(key: Any?): KotlinInnerSpec {
        return applyToSpec { it.key(key) }
    }

    /**
     * Sets multiple keys in the currently referred map property from a Collection.
     *
     * @param keys The Collection of keys to set in the map. Can be empty.
     */
    fun keys(keys: Collection<*>): KotlinInnerSpec {
        return applyToSpec { it.keys(keys) }
    }

    /**
     * Sets multiple keys in the currently referred map property.
     *
     * @param keys The keys to set in the map. Can be empty.
     */
    fun keys(vararg keys: Any?): KotlinInnerSpec {
        return applyToSpec { it.keys(*keys) }
    }

    /**
     * Sets a nested map key within the currently referred map property.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  the nested map key
     */
    fun key(configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.key { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets a value in the currently referred map property.
     *
     * @param value value of the map value to set
     */
    fun value(value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.value(value) }
    }

    /**
     * Sets multiple values in the currently referred map property from a Collection.
     *
     * @param values The Collection of values to set in the map. Can be empty.
     */
    fun values(values: Collection<*>): KotlinInnerSpec {
        return applyToSpec { it.values(values) }
    }

    /**
     * Sets multiple values in the currently referred map property.
     *
     * @param values The values to be added to the map. Can be empty.
     */
    fun values(vararg values: Any?): KotlinInnerSpec {
        return applyToSpec { it.values(*values) }
    }

    /**
     * Sets a nested map value within the currently referred map property.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  the nested map value
     */
    fun value(configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.value { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets an entry in the currently referred map property.
     *
     * @param key   value of the entry key to set
     * @param value value of the entry value to set
     */
    fun entry(key: Any?, value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.entry(key, value) }
    }

    /**
     * Sets multiple key-value pairs in the map from a Collection.
     *
     * @param entries The entries to be added to the map. Should be entered in key, value order. Can be empty.
     */
    fun entries(entries: Collection<*>): KotlinInnerSpec {
        return applyToSpec { it.entries(entries) }
    }

    /**
     * Sets multiple key-value pairs in the map.
     *
     * @param entries The entries to be added to the map. Should be entered in key, value order. Can be empty.
     */
    fun entries(vararg entries: Any?): KotlinInnerSpec {
        return applyToSpec { it.entries(*entries) }
    }

    /**
     * Sets an entry with a specified key within the currently referred map property,
     * and applies a lambda function to configure the value.
     *
     * @param key      value of the map key to set
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  the nested map value
     */
    fun entry(key: Any?, configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.entry(key) { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets an entry with a specified value within the currently referred map property,
     * and applies a lambda function to configure the key.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  the nested map key
     * @param value     value of the map value to set
     */
    fun entry(configure: KotlinInnerSpec.() -> Unit, value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.entry({ nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }, value)
        }
    }

    /**
     * Sets a key in the currently referred map property with a key obtained lazily from the given supplier.
     *
     * @param supplier a supplier function that provides the value of the map key to set.
     */
    fun keyLazy(supplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.keyLazy { supplier() } }
    }

    /**
     * Sets a value in the currently referred map property with a value obtained lazily from the given supplier.
     *
     * @param supplier a supplier function that provides the value of the map value to set.
     */
    fun valueLazy(supplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.valueLazy { supplier() } }
    }

    /**
     * Sets an entry in the currently referred map property with a key and value
     * obtained lazily from the given suppliers.
     *
     * @param keySupplier   a supplier function that provides the value of the map key to set.
     * @param valueSupplier a function that provides the value of the map value to set.
     */
    fun entryLazy(keySupplier: () -> Any?, valueSupplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.entryLazy({ keySupplier() }, { valueSupplier() }) }
    }

    /**
     * Sets every key in the currently referred map property using a lambda function.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  each key in the map property.
     */
    fun allKey(configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.allKey { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets every key in the currently referred map property with a key obtained lazily from the given supplier.
     *
     * @param supplier a supplier function that provides the value of the map keys to set.
     */
    fun allKeyLazy(supplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.allKeyLazy { supplier() } }
    }

    /**
     * Sets every value in the currently referred map property.
     *
     * @param value value of the map value to set
     */
    fun allValue(value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.allValue(value) }
    }

    /**
     * Sets every value in the currently referred map property using a lambda function.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  each value in the map property.
     */
    fun allValue(configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.allValue { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets every value in the currently referred map property with a value obtained lazily from the given supplier.
     *
     * @param supplier a supplier function that provides the value of the map values to set.
     */
    fun allValueLazy(supplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.allValueLazy { supplier() } }
    }

    /**
     * Sets every entry in the currently referred map property with a key
     * obtained lazily from the given supplier and a specified value.
     *
     * @param keySupplier a supplier function that provides the value of the map keys to set.
     * @param value       the value to set
     */
    fun allEntry(keySupplier: () -> Any?, value: Any?): KotlinInnerSpec {
        return applyToSpec { it.allEntry({ keySupplier() }, value) }
    }

    /**
     * Sets every entry in the currently referred map property with a key and value
     * obtained lazily from the given suppliers.
     *
     * @param keySupplier   a supplier function that provides the value of the map keys to set.
     * @param valueSupplier a supplier function that provides the value of the map values to set.
     */
    fun allEntryLazy(keySupplier: () -> Any?, valueSupplier: () -> Any?): KotlinInnerSpec {
        return applyToSpec { it.allEntryLazy({ keySupplier() }, { valueSupplier() }) }
    }

    /**
     * Sets an element at the specified index within the currently referred container property.
     *
     * @param index index of the element to set
     * @param value value of the element to set
     */
    fun listElement(index: Int, value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.listElement(index, value) }
    }

    /**
     * Sets an element at the specified index within the currently referred container property
     * using a lambda function.
     *
     * @param index     index of the element to set
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  the element.
     */
    fun listElement(index: Int, configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.listElement(index) { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets every element within the currently referred container property.
     *
     * @param value value of the elements to set
     */
    fun allListElement(value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.allListElement(value) }
    }

    /**
     * Sets every element within the currently referred container property using a lambda function.
     *
     * @param configure a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                  each element.
     */
    fun allListElement(configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.allListElement { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets a property within the currently referred property.
     *
     * @param propertyName name of the property to set
     *                     (only string-formatted property names are allowed, and expressions are not supported)
     * @param value        value of the property to set
     */
    fun property(propertyName: String, value: @Nullable Any?): KotlinInnerSpec {
        return applyToSpec { it.property(propertyName, value) }
    }

    /**
     * Sets a property within the currently referred property using a lambda function.
     *
     * @param propertyName name of the property to set
     *                     (only string-formatted property names are allowed, and expressions are not supported)
     * @param configure    a lambda function that takes a {@code KotlinInnerSpec} instance and configures
     *                     the nested property.
     */
    fun property(propertyName: String, configure: KotlinInnerSpec.() -> Unit): KotlinInnerSpec {
        return applyToSpec { spec ->
            spec.property(propertyName) { nestedSpec ->
                val nestedKotlinSpec = KotlinInnerSpec()
                nestedKotlinSpec.configure()
                nestedKotlinSpec.directApplyTo(nestedSpec)
            }
        }
    }

    /**
     * Sets the post-condition for the currently referred property.
     *
     * @param type   type of the property to set
     * @param filter a predicate function that determines the post-condition of the property
     */
    fun <T> postCondition(type: Class<T>, filter: Predicate<T>): KotlinInnerSpec {
        return applyToSpec { it.postCondition(type, filter) }
    }

    /**
     * Sets the post-condition for the currently referred property with reified type.
     *
     * @param filter a predicate function that determines the post-condition of the property
     */
    inline fun <reified T> postCondition(noinline filter: (T) -> Boolean): KotlinInnerSpec {
        return postCondition(T::class.java, Predicate(filter))
    }

    /**
     * Convert this KotlinInnerSpec to Java InnerSpec.
     * This creates a new InnerSpec and applies all operations to it.
     * This is the main method used by setKotlinInner extension function.
     */
    fun toInnerSpec(): InnerSpec {
        val javaInnerSpec = InnerSpec()
        directApplyTo(javaInnerSpec)
        return javaInnerSpec
    }

    private fun applyToSpec(operation: (InnerSpec) -> Unit): KotlinInnerSpec {
        operations.add(operation)
        return this
    }

    /**
     * Apply all operations directly to the provided InnerSpec.
     * This is used for nested configurations where we want immediate application.
     */
    private fun directApplyTo(javaInnerSpec: InnerSpec) {
        operations.forEach { operation ->
            operation(javaInnerSpec)
        }
    }
}

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

package com.navercorp.fixturemonkey.kotlin.node

import com.navercorp.objectfarm.api.node.JvmNodePromoter
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL

/**
 * Provides a collection of Kotlin-specific JvmNodePromoters.
 *
 * Usage with TreeResolverPlugin:
 * ```kotlin
 * FixtureMonkey.builder()
 *     .plugin(KotlinPlugin())
 *     .plugin(TreeResolverPlugin()
 *         .nodePromoters(KotlinNodePromoters.all()))
 *     .build()
 * ```
 *
 * Or add individual promoters:
 * ```kotlin
 * FixtureMonkey.builder()
 *     .plugin(KotlinPlugin())
 *     .plugin(TreeResolverPlugin()
 *         .nodePromoter(KotlinNodePromoters.interfacePromoter())
 *         .nodePromoter(KotlinNodePromoters.objectPromoter()))
 *     .build()
 * ```
 */
@API(since = "1.1.0", status = EXPERIMENTAL)
object KotlinNodePromoters {

    /**
     * Returns all Kotlin-specific node promoters in the recommended order.
     *
     * The order is:
     * 1. KotlinInterfaceNodePromoter - handles sealed classes and Kotlin interfaces
     * 2. KotlinNodePromoter - handles concrete Kotlin types
     *
     * @return list of all Kotlin promoters
     */
    @JvmStatic
    fun all(): List<JvmNodePromoter> = listOf(
        KotlinInterfaceNodePromoter(),
        KotlinNodePromoter()
    )

    /**
     * Returns the Kotlin interface/sealed class promoter.
     *
     * This promoter handles:
     * - Kotlin sealed classes (resolves to one of the subclasses)
     * - Kotlin interfaces and abstract classes
     *
     * @return the Kotlin interface node promoter
     */
    @JvmStatic
    fun interfacePromoter(): JvmNodePromoter = KotlinInterfaceNodePromoter()

    /**
     * Returns the Kotlin object promoter.
     *
     * This promoter handles concrete Kotlin types and preserves:
     * - Nullability information
     * - Default parameter values
     * - Value class detection
     *
     * @return the Kotlin object node promoter
     */
    @JvmStatic
    fun objectPromoter(): JvmNodePromoter = KotlinNodePromoter()
}

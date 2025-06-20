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

import com.navercorp.fixturemonkey.adapter.DefaultNodeTreeAdapter
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.kotlin.generator.KotlinNullInjectGenerator
import com.navercorp.fixturemonkey.kotlin.node.KotlinLeafTypeResolver
import com.navercorp.fixturemonkey.kotlin.node.KotlinNodeCandidateGenerator
import com.navercorp.fixturemonkey.kotlin.node.KotlinNodePromoters
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL

/**
 * A plugin for configuring the node tree adapter for Kotlin types in FixtureMonkey.
 *
 * This plugin enables the adapter layer for Kotlin types, which provides
 * an alternative object generation path using JvmNodeTree.
 *
 * Usage:
 * ```kotlin
 * FixtureMonkey.builder()
 *     .plugin(KotlinPlugin())
 *     .plugin(KotlinNodeTreeAdapterPlugin()
 *         .seed(12345L)
 *         .tracer(AdapterTracer.console()))
 *     .build()
 * ```
 *
 * @since 1.2.0
 */
@API(since = "1.2.0", status = EXPERIMENTAL)
class KotlinNodeTreeAdapterPlugin : Plugin {
    private var enabled = true
    private var seed = System.nanoTime()
    private var tracer: AdapterTracer = AdapterTracer.noOp()

    /**
     * Enables or disables the node tree adapter.
     *
     * When enabled, the [DefaultNodeTreeAdapter] will be used
     * for object generation using JvmNodeTree.
     *
     * @param enabled true to enable the adapter, false to disable
     * @return this plugin for method chaining
     */
    fun enabled(enabled: Boolean): KotlinNodeTreeAdapterPlugin {
        this.enabled = enabled
        return this
    }

    /**
     * Sets the seed for random value generation in the adapter.
     *
     * Using a fixed seed value ensures reproducible test results.
     *
     * @param seed the seed value for random generation
     * @return this plugin for method chaining
     */
    fun seed(seed: Long): KotlinNodeTreeAdapterPlugin {
        this.seed = seed
        return this
    }

    /**
     * Sets the tracer for debugging resolution process.
     *
     * The tracer receives detailed information about how values are resolved
     * during fixture generation, including manipulator analysis and assembly steps.
     *
     * @param tracer the tracer to use for debugging
     * @return this plugin for method chaining
     * @see AdapterTracer.console
     * @see AdapterTracer.consoleJson
     */
    fun tracer(tracer: AdapterTracer): KotlinNodeTreeAdapterPlugin {
        this.tracer = tracer
        return this
    }

    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        if (enabled) {
            optionsBuilder.nodeTreeAdapter(
                DefaultNodeTreeAdapter(
                    seed,
                    JvmNodeCandidateTreeContext(),
                    KotlinNodePromoters.all(),
                    listOf(KotlinLeafTypeResolver.INSTANCE),
                    { delegate -> KotlinNodeCandidateGenerator(delegate) }
                )
            )
            optionsBuilder.defaultNullInjectGeneratorOperator { delegate ->
                KotlinNullInjectGenerator(delegate)
            }
        }
        optionsBuilder.adapterTracer(tracer)
    }
}

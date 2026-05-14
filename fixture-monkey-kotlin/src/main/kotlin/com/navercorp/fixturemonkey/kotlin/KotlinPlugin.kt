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

import com.navercorp.fixturemonkey.adapter.AssemblyPlanner
import com.navercorp.fixturemonkey.api.generator.FunctionalInterfaceContainerPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.MatchPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator
import com.navercorp.fixturemonkey.api.introspector.FunctionalInterfaceArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.plugin.JvmTypeSystem
import com.navercorp.fixturemonkey.plugin.JvmTypeSystemPlugin
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.generator.InterfaceKFunctionPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.generator.PairContainerPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.generator.PairDecomposedContainerValueFactory
import com.navercorp.fixturemonkey.kotlin.generator.TripleContainerPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.generator.TripleDecomposedContainerValueFactory
import com.navercorp.fixturemonkey.kotlin.instantiator.KotlinInstantiatorProcessor
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinDurationIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.PairIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.TripleIntrospector
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.DURATION_TYPE_MATCHER
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.PAIR_TYPE_MATCHER
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.TRIPLE_TYPE_MATCHER
import com.navercorp.fixturemonkey.kotlin.generator.KotlinNullInjectGenerator
import com.navercorp.fixturemonkey.kotlin.node.KotlinLeafTypeResolver
import com.navercorp.fixturemonkey.kotlin.node.KotlinNodeCandidateGenerator
import com.navercorp.fixturemonkey.kotlin.node.KotlinNodePromoters
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.KotlinNullabilityUtils
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import com.navercorp.fixturemonkey.kotlin.type.isKotlinLambda
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import java.lang.reflect.Modifier

@API(since = "0.4.0", status = MAINTAINED)
class KotlinPlugin : Plugin, JvmTypeSystemPlugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        optionsBuilder.objectIntrospector {
            MatchArbitraryIntrospector(
                listOf(
                    PrimaryConstructorArbitraryIntrospector.INSTANCE,
                    it
                )
            )
        }
            .defaultPropertyGenerator(
                MatchPropertyGenerator(
                    listOf(
                        MatcherOperator(
                            { property -> property.jvmType.rawType.isKotlinType() },
                            KotlinPropertyGenerator()
                        ),
                        MatcherOperator({ true }, DefaultPropertyGenerator())
                    )
                )
            )
            .insertFirstArbitraryContainerPropertyGenerator(
                { property -> property.jvmType.rawType.cachedKotlin().isKotlinLambda() }
            ) { FunctionalInterfaceContainerPropertyGenerator.INSTANCE.generate(it) }
            .insertFirstArbitraryIntrospector(
                { property -> property.jvmType.rawType.cachedKotlin().isKotlinLambda() },
                FunctionalInterfaceArbitraryIntrospector()
            )
            .insertFirstCandidateConcretePropertyResolvers(
                MatcherOperator(
                    { it.jvmType.rawType.cachedKotlin().isSealed },
                    CandidateConcretePropertyResolver { property ->
                        ConcreteTypeCandidateConcretePropertyResolver(
                            property.jvmType.rawType.cachedKotlin().sealedSubclasses
                                .map { it.java }
                        )
                            .resolve(property)
                    }
                ),
            )
            .insertFirstPropertyGenerator(
                MatcherOperator(
                    { p -> Modifier.isInterface(p.jvmType.rawType.modifiers) },
                    InterfaceKFunctionPropertyGenerator(),
                ),
            )
            .insertFirstArbitraryIntrospector(
                DURATION_TYPE_MATCHER,
                KotlinDurationIntrospector(),
            )
            .insertFirstArbitraryContainerPropertyGenerator(
                PAIR_TYPE_MATCHER,
                PairContainerPropertyGenerator(),
            )
            .insertFirstArbitraryContainerPropertyGenerator(
                TRIPLE_TYPE_MATCHER,
                TripleContainerPropertyGenerator(),
            )
            .containerIntrospector {
                MatchArbitraryIntrospector(
                    listOf(
                        PairIntrospector(),
                        TripleIntrospector(),
                        it,
                    ),
                )
            }
            .addDecomposedContainerValueFactory(
                Pair::class.java,
                PairDecomposedContainerValueFactory(),
            )
            .addDecomposedContainerValueFactory(
                Triple::class.java,
                TripleDecomposedContainerValueFactory(),
            )
            .instantiatorProcessor(KotlinInstantiatorProcessor())
            // Register null inject generator for Kotlin non-nullable types (adapter path)
            .insertFirstNullInjectGenerators(
                MatcherOperator(
                    { property -> isKotlinNonNullableProperty(property) },
                    NullInjectGenerator { 0.0 }
                )
            )
            .defaultNullInjectGeneratorOperator { delegate ->
                KotlinNullInjectGenerator(delegate)
            }
    }

    override fun configure(typeSystem: JvmTypeSystem) {
        typeSystem.assemblyPlanner(
            AssemblyPlanner(
                System.nanoTime(),
                JvmNodeCandidateTreeContext(),
                KotlinNodePromoters.all(),
                listOf(KotlinLeafTypeResolver.INSTANCE),
                { delegate -> KotlinNodeCandidateGenerator(delegate) }
            )
        )
    }

    private fun isKotlinNonNullableProperty(property: com.navercorp.fixturemonkey.api.property.Property): Boolean {
        // Check for adapter path (ConstructorProperty from Kotlin class)
        if (property is com.navercorp.fixturemonkey.api.property.ConstructorProperty) {
            val constructor = property.constructor
            if (com.navercorp.fixturemonkey.api.type.KotlinTypeDetector.isKotlinType(constructor.declaringClass)) {
                return !KotlinNullabilityUtils.isNullableConstructorParamByName(constructor, property.name)
            }
        }

        // Check for adapter path (FieldProperty from Kotlin class)
        if (property is com.navercorp.fixturemonkey.api.property.FieldProperty) {
            val field = property.field
            if (com.navercorp.fixturemonkey.api.type.KotlinTypeDetector.isKotlinType(field.declaringClass)) {
                return !KotlinNullabilityUtils.isNullableField(field)
            }
        }

        // Check for legacy path (regular Kotlin property with isNullable)
        val nullable = property.isNullable()
        if (nullable == false) {
            return true
        }

        return false
    }
}

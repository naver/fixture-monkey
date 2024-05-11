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

import com.navercorp.fixturemonkey.api.generator.InterfaceObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
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
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.type.actualType
import com.navercorp.fixturemonkey.kotlin.type.cachedKotlin
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import java.lang.reflect.Modifier

@API(since = "0.4.0", status = MAINTAINED)
class KotlinPlugin : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        optionsBuilder.objectIntrospector {
            MatchArbitraryIntrospector(
                listOf(
                    PrimaryConstructorArbitraryIntrospector.INSTANCE,
                    it
                )
            )
        }
            .defaultPropertyGenerator(KotlinPropertyGenerator())
            .insertFirstArbitraryObjectPropertyGenerator(
                MatcherOperator(
                    { it.type.actualType().cachedKotlin().isSealed },
                    ObjectPropertyGenerator { context ->
                        InterfaceObjectPropertyGenerator(
                            context.property.type.actualType().cachedKotlin().sealedSubclasses
                                .filter { it.objectInstance == null }
                                .map { it.java },
                        )
                            .generate(context)
                    },
                ),
            )
            .insertFirstPropertyGenerator(
                MatcherOperator(
                    { p -> Modifier.isInterface(p.type.actualType().modifiers) },
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
    }
}

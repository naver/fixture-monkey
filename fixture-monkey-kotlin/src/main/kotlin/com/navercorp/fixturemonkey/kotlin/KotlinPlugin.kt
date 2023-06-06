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
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.generator.InterfaceKFunctionPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.MAINTAINED
import java.lang.reflect.Modifier

@API(since = "0.4.0", status = MAINTAINED)
class KotlinPlugin : Plugin {
    override fun accept(optionsBuilder: GenerateOptionsBuilder) {
        optionsBuilder.objectIntrospector { PrimaryConstructorArbitraryIntrospector.INSTANCE }
            .defaultPropertyGenerator(KotlinPropertyGenerator())
            .insertFirstArbitraryObjectPropertyGenerator(
                MatcherOperator(
                    { (Types.getActualType(it.type) as Class<*>).kotlin.isSealed },
                    ObjectPropertyGenerator { context ->
                        InterfaceObjectPropertyGenerator(
                            (Types.getActualType(context.property.type) as Class<*>).kotlin.sealedSubclasses
                                .filter { it.objectInstance == null }
                                .map { it.java },
                        )
                            .generate(context)
                    },
                ),
            )
            .insertFirstPropertyGenerator(
                MatcherOperator(
                    { p -> Modifier.isInterface(Types.getActualType(p.type).modifiers) },
                    InterfaceKFunctionPropertyGenerator(),
                ),
            )
    }
}

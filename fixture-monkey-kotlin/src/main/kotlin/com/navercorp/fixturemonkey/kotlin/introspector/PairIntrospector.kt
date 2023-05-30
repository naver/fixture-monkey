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

package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import net.jqwik.api.Combinators
import org.apiguardian.api.API

@API(since = "0.6.0", status = API.Status.EXPERIMENTAL)
class PairIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = AssignableTypeMatcher(Pair::class.java)

    override fun match(property: Property?): Boolean { return MATCHER.match(property) }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty
        val containerProperty = property.containerProperty
            ?: throw IllegalArgumentException(
                "container property should not null. type: ${property.objectProperty.property.name}"
            )

        if (containerProperty.containerInfo == null) {
            return ArbitraryIntrospectorResult.EMPTY
        }

        val childrenArbitraries = context.elementArbitraries.map { it.combined() }

        if (childrenArbitraries.size != 2) {
            throw IllegalArgumentException("First and Second value should exist for PairType.")
        }

        return ArbitraryIntrospectorResult(
            Combinators.combine(childrenArbitraries[0], childrenArbitraries[1])
                .`as` { t1, t2 -> Pair(t1, t2) }
        )
    }
}

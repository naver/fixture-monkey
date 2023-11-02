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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.PAIR_TYPE_MATCHER
import org.apiguardian.api.API
import org.apiguardian.api.API.Status

@API(since = "0.6.0", status = Status.MAINTAINED)
class PairIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = PAIR_TYPE_MATCHER

    override fun match(property: Property?): Boolean {
        return MATCHER.match(property)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty

        if (!property.isContainer) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        val elementCombinableArbitraryList = context.elementCombinableArbitraryList

        if (elementCombinableArbitraryList.size != 2) {
            throw IllegalArgumentException("First and Second value should exist for PairType.")
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.containerBuilder()
                .elements(elementCombinableArbitraryList)
                .build { Pair(it[0], it[1]) },
        )
    }
}

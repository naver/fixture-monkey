package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers.TRIPLE_TYPE_MATCHER
import org.apiguardian.api.API
import org.apiguardian.api.API.Status

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
class TripleIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = TRIPLE_TYPE_MATCHER

    override fun match(property: Property?): Boolean {
        return MATCHER.match(property)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty

        if (!property.isContainer) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        val elementCombinableArbitraryList = context.elementCombinableArbitraryList

        if (elementCombinableArbitraryList.size != 3) {
            throw IllegalArgumentException("First, second, and third value should exist for TripleType.")
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.containerBuilder()
                .elements(elementCombinableArbitraryList)
                .build { Triple(it[0], it[1], it[2]) },
        )
    }
}

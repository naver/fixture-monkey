package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.kotlin.matcher.Matchers
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SEED = 234L

class DurationIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = Matchers.DURATION_TYPE_MATCHER

    override fun match(property: Property?): Boolean {
        return MATCHER.match(property)
    }

    override fun introspect(context: ArbitraryGeneratorContext?): ArbitraryIntrospectorResult {
        val property = context!!.arbitraryProperty

        if (!property.isContainer) {
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }

        require(match(context.resolvedProperty)) { "Given type is not Boolean. type: " + context.resolvedType }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.from { Random(SEED).nextLong().toDuration(DurationUnit.NANOSECONDS) }
        )
    }
}

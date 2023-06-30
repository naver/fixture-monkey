package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.matcher.TripleGenericTypeMatcher
import com.navercorp.fixturemonkey.api.property.Property
import org.apiguardian.api.API
import org.apiguardian.api.API.Status

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
class TripleIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = Matcher { property ->
        AssignableTypeMatcher(Triple::class.java).match(property) && TripleGenericTypeMatcher().match(property)
    }

    override fun match(property: Property?): Boolean {
        return MATCHER.match(property)
    }

    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val property = context.arbitraryProperty
        val containerProperty = property.containerProperty
            ?: throw IllegalArgumentException(
                "container property should not null. type: ${property.objectProperty.property.name}"
            )

        if (containerProperty.containerInfo == null) {
            return ArbitraryIntrospectorResult.EMPTY
        }

        val elementCombinableArbitraryList = context.elementCombinableArbitraryList

        if (elementCombinableArbitraryList.size != 3) {
            throw IllegalArgumentException("First, second, and third value should exist for TripleType.")
        }

        return ArbitraryIntrospectorResult(
            CombinableArbitrary.containerBuilder()
                .elements(elementCombinableArbitraryList)
                .build { Triple(it[0], it[1], it[2]) }
        )
    }
}

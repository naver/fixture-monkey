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
class TripleIntrospector : ArbitraryIntrospector, Matcher {
    private val MATCHER = AssignableTypeMatcher(Triple::class.java)

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

        if (childrenArbitraries.size != 3) {
            throw IllegalArgumentException("First, second, and third value should exist for TripleType.")
        }

        return ArbitraryIntrospectorResult(
            Combinators.combine(childrenArbitraries[0], childrenArbitraries[1], childrenArbitraries[2])
                .`as` { t1, t2, t3 -> Triple(t1, t2, t3) }
        )
    }
}

package com.navercorp.fixturemonkey.kotlin.matcher

import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.type.Types
import kotlin.time.Duration

class DurationKTypeMatcher : Matcher {
    override fun match(property: Property): Boolean {
        return Duration::class.java.isAssignableFrom(Types.getActualType(property.type))
    }
}

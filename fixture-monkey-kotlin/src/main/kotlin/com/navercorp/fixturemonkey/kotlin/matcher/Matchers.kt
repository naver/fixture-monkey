package com.navercorp.fixturemonkey.kotlin.matcher

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.DoubleGenericTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.matcher.TripleGenericTypeMatcher

object Matchers {
    val PAIR_TYPE_MATCHER = Matcher { property ->
        AssignableTypeMatcher(Pair::class.java).match(property) && DoubleGenericTypeMatcher().match(property)
    }

    val TRIPLE_TYPE_MATCHER = Matcher { property ->
        AssignableTypeMatcher(Triple::class.java).match(property) && TripleGenericTypeMatcher().match(property)
    }
}

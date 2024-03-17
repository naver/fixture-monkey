package com.navercorp.fixturemonkey.kotlin.matcher

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.DoubleGenericTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.TripleGenericTypeMatcher
import kotlin.time.Duration

object Matchers {
    val PAIR_TYPE_MATCHER = AssignableTypeMatcher(Pair::class.java).intersect(DoubleGenericTypeMatcher())

    val TRIPLE_TYPE_MATCHER = AssignableTypeMatcher(Triple::class.java).intersect(TripleGenericTypeMatcher())

    val DURATION_TYPE_MATCHER = AssignableTypeMatcher(Duration::class.java)
}

package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary
import java.util.function.Predicate

/**
 * StringCombinableArbitrary의 공통 로직을 처리하는 베이스 클래스
 */
abstract class BaseStringCombinableArbitrary : StringCombinableArbitrary {
    private var minLength: Int = 1
    private var maxLength: Int = 10
    private var isFixed: Boolean = false
    private var filter: Predicate<Char> = Predicate { true }

    override fun withLength(min: Int, max: Int): StringCombinableArbitrary {
        this.minLength = min
        this.maxLength = max
        return this
    }

    override fun alphabetic(): StringCombinableArbitrary {
        this.filter = Predicate { it.isLetter() }
        return this
    }

    override fun ascii(): StringCombinableArbitrary {
        this.filter = Predicate { it.code in 32..126 }
        return this
    }

    override fun numeric(): StringCombinableArbitrary {
        this.filter = Predicate { it.isDigit() }
        return this
    }

    override fun korean(): StringCombinableArbitrary {
        this.filter = Predicate { it in '\uAC00'..'\uD7AF' }
        return this
    }

    override fun filterCharacter(tries: Int, predicate: Predicate<Char>): StringCombinableArbitrary {
        this.filter = predicate
        return this
    }

    override fun combined(): String {
        val raw = rawValue()
        val filtered = raw.filter(filter::test)

        return if (isFixed) {
            filtered.padEnd(minLength, ' ')
        } else {
            filtered.take(maxLength).padEnd(minLength, ' ')
        }
    }

    override fun rawValue(): String {
        return (1..maxLength).map { ('a'..'z').random() }.joinToString("")
    }

    override fun clear() {
        minLength = 1
        maxLength = 10
        isFixed = false
        filter = Predicate { true }
    }

    override fun fixed(): Boolean = isFixed
}

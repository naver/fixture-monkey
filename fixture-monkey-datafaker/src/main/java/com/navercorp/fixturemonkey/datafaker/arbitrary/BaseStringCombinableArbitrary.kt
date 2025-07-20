package com.navercorp.fixturemonkey.datafaker.arbitrary

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import java.util.function.Function

abstract class BaseStringCombinableArbitrary : CombinableArbitrary<String> {
    override fun rawValue(): Any = combined()

    override fun clear() {
    }

    override fun fixed(): Boolean = false

    override fun <R> map(mapper: Function<String, R>): CombinableArbitrary<R> {
        return CombinableArbitrary.from { mapper.apply(this.combined()) }
    }

    override fun filter(predicate: java.util.function.Predicate<String>): CombinableArbitrary<String> {
        return CombinableArbitrary.from(this::combined).filter(predicate)
    }

    override fun unique(): CombinableArbitrary<String> {
        return CombinableArbitrary.from(this::combined).unique()
    }

}

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

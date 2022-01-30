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

package com.navercorp.fixturemonkey.kotlin

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ExpressionGeneratorTest {
    @Test
    fun getExpressionField() {
        // given
        val generator = Exp<Person>() into Person::dogs

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getExpressionNestedField() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getExpressionNestedFieldWithIndex() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getExpressionNestedFieldWithAllIndex() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::loves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getExpressionListWithIndexOnce() {
        // given
        val generator = Exp<Person>() into Person::dogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getExpressionListWithAllIndexOnce() {
        // given
        val generator = Exp<Person>() into Person::dogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getExpressionListWithIndexAndAllIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs[1]["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getExpressionListWithAllIndexAndIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs["*"][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getExpressionListWithAllIndexTwiceWithField() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs["*"]["*"] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getExpressionListWithIndexTwiceWithFieldDiffExpression1() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs[1][2] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionListWithIndexTwiceWithFieldDiffExpression2() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs get 1 get 2 into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionListWithIndexTwiceWithFieldDiffExpression3() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedDogs)[1][2]
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionListWithIndexTwiceWithFieldDiffExpression4() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedDogs[1][2])
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionFieldWithIndexThrice() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedThriceDogs[1][2][2])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getExpressionFieldWithIndexThriceWithField() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedThriceDogs[1][2][2])
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }
}

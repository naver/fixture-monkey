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

import com.navercorp.fixturemonkey.kotlin.ExpressionGeneratorJavaTestSpecs.DogJava
import com.navercorp.fixturemonkey.kotlin.ExpressionGeneratorJavaTestSpecs.PersonJava
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ExpressionGeneratorsTest {
    @Test
    fun getPropertyExpressionNestedField() {
        // given
        val generator = Person::dog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithIndex() {
        // given
        val generator = Person::dog into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithAllIndex() {
        // given
        val generator = Person::dog into Dog::loves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getPropertyExpressionListWithIndexOnce() {
        // given
        val generator = Person::dogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexOnce() {
        // given
        val generator = Person::dogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getPropertyExpressionNestedListWithIndex() {
        // given
        val generator = Person::nestedDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1]")
    }

    @Test
    fun getPropertyExpressionNestedListWithAllIndex() {
        // given
        val generator = Person::nestedDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*]")
    }

    @Test
    fun getPropertyExpressionListWithIndexAndAllIndex() {
        // given
        val generator = Person::nestedDogs[1]["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexAndIndex() {
        // given
        val generator = Person::nestedDogs["*"][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexTwiceWithField() {
        // given
        val generator = Person::nestedDogs["*"]["*"] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getPropertyExpressionListWithIndexTwiceWithFieldDiffExpression1() {
        // given
        val generator = Person::nestedDogs[1][2] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThrice() {
        // given
        val generator = Person::nestedThriceDogs[1][2][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThriceWithField() {
        // given
        val generator = Person::nestedThriceDogs[1][2][2] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }

    @Test
    fun getGetterExpressionNestedField() {
        // given
        val generator = PersonJava::getDog intoGetter DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getGetterExpressionNestedFieldWithIndex() {
        // given
        val generator = PersonJava::getDog into DogJava::getLoves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getGetterExpressionNestedFieldWithAllIndex() {
        // given
        val generator = PersonJava::getDog into DogJava::getLoves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getGetterExpressionListWithIndexOnce() {
        // given
        val generator = PersonJava::getDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexOnce() {
        // given
        val generator = PersonJava::getDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getGetterExpressionNestedListWithIndex() {
        // given
        val generator = PersonJava::getNestedDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1]")
    }

    @Test
    fun getGetterExpressionNestedListWithAllIndex() {
        // given
        val generator = PersonJava::getNestedDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*]")
    }

    @Test
    fun getGetterExpressionListWithIndexAndAllIndex() {
        // given
        val generator = PersonJava::getNestedDogs[1]["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexAndIndex() {
        // given
        val generator = PersonJava::getNestedDogs["*"][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexTwiceWithField() {
        // given
        val generator = PersonJava::getNestedDogs["*"]["*"] intoGetter DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getGetterExpressionListWithIndexTwiceWithFieldDiffExpression1() {
        // given
        val generator = PersonJava::getNestedDogs[1][2] intoGetter DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getGetterExpressionFieldWithIndexThrice() {
        // given
        val generator = PersonJava::getNestedThriceDogs[1][2][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getGetterExpressionFieldWithIndexThriceWithField() {
        // given
        val generator = PersonJava::getNestedThriceDogs[1][2][2] intoGetter DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }

    @Test
    fun getterNullableField() {
        // given
        val generator = PersonJava::getNullableDog intoGetter DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun getterNullableNestedField() {
        // given
        val generator = PersonJava::getNullableDog intoGetter DogJava::getNullableName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun getterNullableList() {
        // given
        val generator = PersonJava::getNullableDogs[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun propertyNullableField() {
        // given
        val generator = Person::nullableDog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun propertyNullableNestedField() {
        // given
        val generator = Person::nullableDog into Dog::nullableName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun propertyNullableList() {
        // given
        val generator = Person::nullableDogs[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun propertyListCombinesList() {
        // given
        val generator = Person::dogs[0] into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }

    @Test
    fun getterListCombinesList() {
        // given
        val generator = PersonJava::getDogs[0] into DogJava::getLoves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }
}

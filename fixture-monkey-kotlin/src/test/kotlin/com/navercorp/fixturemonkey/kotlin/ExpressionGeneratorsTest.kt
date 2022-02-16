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
    fun getPropertyExpressionField() {
        // given
        val generator = Exp<Person>() into Person::dogs

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getPropertyExpressionFieldWithConstructor() {
        // given
        val generator = Exp(Person::dog)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog")
    }

    @Test
    fun getPropertyExpressionNestedField() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithConstructor() {
        // given
        val generator = Exp(Person::dog into Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithoutExp() {
        // given
        val generator = Person::dog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithIndex() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithIndexWithConstructor() {
        // given
        val generator = Exp<Person>(Person::dog into Dog::loves[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithIndexWithoutExp() {
        // given
        val generator = Person::dog into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithAllIndex() {
        // given
        val generator = Exp<Person>() into Person::dog into Dog::loves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithAllIndexWithConstructor() {
        // given
        val generator = Exp(Person::dog into Dog::loves["*"])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getPropertyExpressionNestedFieldWithAllIndexWithoutExp() {
        // given
        val generator = Person::dog into Dog::loves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getPropertyExpressionListWithIndexOnce() {
        // given
        val generator = Exp<Person>() into Person::dogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getPropertyExpressionListWithIndexOnceWithConstructor() {
        // given
        val generator = Exp(Person::dogs[1])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexOnce() {
        // given
        val generator = Exp<Person>() into Person::dogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexOnceWithConstructor() {
        // given
        val generator = Exp(Person::dogs["*"])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getPropertyExpressionNestedListWithIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1]")
    }

    @Test
    fun getPropertyExpressionNestedListWithIndexWithConstructor() {
        // given
        val generator = Exp(Person::nestedDogs[1])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1]")
    }

    @Test
    fun getPropertyExpressionNestedListWithAllIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*]")
    }

    @Test
    fun getPropertyExpressionNestedListWithAllIndexWithConstructor() {
        // given
        val generator = Exp(Person::nestedDogs["*"])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*]")
    }

    @Test
    fun getPropertyExpressionListWithIndexAndAllIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs[1]["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getPropertyExpressionListWithIndexAndAllIndexWithConstructor() {
        // given
        val generator = Exp(Person::nestedDogs[1]["*"])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexAndIndex() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs["*"][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexAndIndexWithConstructor() {
        // given
        val generator = Exp(Person::nestedDogs["*"][2])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexTwiceWithField() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs["*"]["*"] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexTwiceWithFieldWithConstructor() {
        // given
        val generator = Exp(Person::nestedDogs["*"]["*"] into Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getPropertyExpressionListWithAllIndexTwiceWithFieldWithoutExp() {
        // given
        val generator = Person::nestedDogs["*"]["*"] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getPropertyExpressionListWithIndexTwiceWithFieldDiffExpression1() {
        // given
        val generator = Exp<Person>() into Person::nestedDogs[1][2] into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getPropertyExpressionListWithIndexTwiceWithFieldDiffExpression2() {
        // given
        val generator = Exp<Person>() into (Person::nestedDogs get 1 get 2) into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getPropertyExpressionListWithIndexTwiceWithFieldDiffExpression3() {
        // given
        val generator = Exp<Person>()
            .into((Person::nestedDogs)[1][2])
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getPropertyExpressionListWithIndexTwiceWithFieldDiffExpression4() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedDogs[1][2])
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThrice() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedThriceDogs[1][2][2])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThriceWithConstructor() {
        // given
        val generator = Exp(Person::nestedThriceDogs[1][2][2])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThriceWithField() {
        // given
        val generator = Exp<Person>()
            .into(Person::nestedThriceDogs[1][2][2])
            .into(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }

    @Test
    fun getPropertyExpressionFieldWithIndexThriceWithFieldWithConstructor() {
        // given
        val generator = Exp(Person::nestedThriceDogs[1][2][2] into Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }

    //
    @Test
    fun getGetterExpressionField() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDogs

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getGetterExpressionFieldWithConstructor() {
        // given
        val generator = Exp(PersonJava::getDogs)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getGetterExpressionNestedField() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDog into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getGetterExpressionNestedFieldWithConstructor() {
        // given
        val generator = Exp(PersonJava::getDog into DogJava::getName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getGetterExpressionNestedFieldWithoutExp() {
        // given
        val generator = PersonJava::getDog into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getGetterExpressionNestedFieldWithIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDog into DogJava::getLoves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getGetterExpressionNestedFieldWithIndexWithConstructor() {
        // given
        val generator = Exp(PersonJava::getDog into DogJava::getLoves[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getGetterExpressionNestedFieldWithAllIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDog into DogJava::getLoves["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getGetterExpressionNestedFieldWithAllIndexWithConstructor() {
        // given
        val generator = Exp(PersonJava::getDog into DogJava::getLoves["*"])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[*]")
    }

    @Test
    fun getGetterExpressionListWithIndexOnce() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexOnce() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getGetterExpressionNestedListWithIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1]")
    }

    @Test
    fun getGetterExpressionNestedListWithAllIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*]")
    }

    @Test
    fun getGetterExpressionListWithIndexAndAllIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs[1]["*"]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][*]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexAndIndex() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs["*"][2]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][2]")
    }

    @Test
    fun getGetterExpressionListWithAllIndexTwiceWithField() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs["*"]["*"] into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getGetterExpressionListWithIndexTwiceWithFieldDiffExpression1() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNestedDogs[1][2] into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getGetterExpressionListWithIndexTwiceWithFieldDiffExpression2() {
        // given
        val generator = Exp<PersonJava>() into (PersonJava::getNestedDogs get 1 get 2) into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getGetterExpressionListWithIndexTwiceWithFieldDiffExpression3() {
        // given
        val generator = Exp<PersonJava>()
            .into((PersonJava::getNestedDogs)[1][2])
            .into(DogJava::getName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getGetterExpressionListWithIndexTwiceWithFieldDiffExpression4() {
        // given
        val generator = Exp<PersonJava>()
            .into(PersonJava::getNestedDogs[1][2])
            .into(DogJava::getName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getGetterExpressionFieldWithIndexThrice() {
        // given
        val generator = Exp<PersonJava>()
            .into(PersonJava::getNestedThriceDogs[1][2][2])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2]")
    }

    @Test
    fun getGetterExpressionFieldWithIndexThriceWithField() {
        // given
        val generator = Exp<PersonJava>()
            .into(PersonJava::getNestedThriceDogs[1][2][2])
            .into(DogJava::getName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedThriceDogs[1][2][2].name")
    }

    @Test
    fun notGetter() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::notGetter

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("notGetter")
    }

    @Test
    fun getterNullableField() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNullableDog into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun getterNullableFieldWithConstructor() {
        // given
        val generator = Exp(PersonJava::getNullableDog into DogJava::getName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun getterNullableFieldWithoutExp() {
        // given
        val generator = PersonJava::getNullableDog into DogJava::getName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun getterNullableNestedField() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNullableDog into DogJava::getNullableName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun getterNullableNestedFieldWithConstructor() {
        // given
        val generator = Exp(PersonJava::getNullableDog into DogJava::getNullableName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun getterNullableList() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getNullableDogs[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun getterNullableListWithConstructor() {
        // given
        val generator = Exp(PersonJava::getNullableDogs[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun propertyNullableField() {
        // given
        val generator = Exp<Person>() into Person::nullableDog into Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun propertyNullableFieldWithConstructor() {
        // given
        val generator = Exp(Person::nullableDog into Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.name")
    }

    @Test
    fun propertyNullableNestedField() {
        // given
        val generator = Exp<Person>() into Person::nullableDog into Dog::nullableName

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun propertyNullableNestedFieldWithConstructor() {
        // given
        val generator = Exp(Person::nullableDog into Dog::nullableName)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDog.nullableName")
    }

    @Test
    fun propertyNullableList() {
        // given
        val generator = Exp<Person>() into Person::nullableDogs[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun propertyNullableListWithConstructor() {
        // given
        val generator = Exp(Person::nullableDogs[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nullableDogs[0]")
    }

    @Test
    fun propertyListCombinesListWithConstructor() {
        // given
        val generator = Exp(Person::dogs[0] into Dog::loves[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }

    @Test
    fun propertyListCombinesListWithoutExp() {
        // given
        val generator = Person::dogs[0] into Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }

    @Test
    fun propertyBoolean() {
        // given
        val generator = Exp<Person>() into Person::married

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("married")
    }

    @Test
    fun propertyBooleanWithConstructor() {
        // given
        val generator = Exp(Person::married)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("married")
    }

    @Test
    fun propertyNullableBoolean() {
        // given
        val generator = Exp<Person>() into Person::happy

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("happy")
    }

    @Test
    fun propertyNullableBooleanWithConstructor() {
        // given
        val generator = Exp(Person::happy)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("happy")
    }

    @Test
    fun getterBoolean() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::isMarried

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("married")
    }

    @Test
    fun getterBooleanWithConstructor() {
        // given
        val generator = Exp(PersonJava::isMarried)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("married")
    }

    @Test
    fun getterNullableBoolean() {
        // given
        val generator = Exp<PersonJava>() into PersonJava::getHappy

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("happy")
    }

    @Test
    fun getterNullableBooleanWithConstructor() {
        // given
        val generator = Exp(PersonJava::getHappy)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("happy")
    }

    @Test
    fun getterListCombinesListWithConstructor() {
        // given
        val generator = Exp(PersonJava::getDogs[0] into DogJava::getLoves[0])

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }

    @Test
    fun getterListCombinesListWithoutExp() {
        // given
        val generator = PersonJava::getDogs[0] into DogJava::getLoves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[0].loves[0]")
    }
}

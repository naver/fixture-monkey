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

    data class Person(
        val name: String?,
        val dog: Dog,
        val dogs: List<Dog>,
        val nestedDogs: List<List<Dog>>,
        val nestedThriceDogs: List<List<List<Dog>>>,
    )

    data class Dog(val name: String, val loves: List<Int>)
}

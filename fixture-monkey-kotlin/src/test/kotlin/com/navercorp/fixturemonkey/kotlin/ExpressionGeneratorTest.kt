package com.navercorp.fixturemonkey.kotlin

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ExpressionGeneratorTest {
    @Test
    fun getExpressionField() {
        // given
        val generator = Exp<Person>() on Person::dogs

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getExpressionNestedField() {
        // given
        val generator = Exp<Person>() on Person::dog on Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getExpressionNestedFieldWithIndex() {
        // given
        val generator = Exp<Person>() on Person::dog on Dog::loves[0]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.loves[0]")
    }

    @Test
    fun getExpressionFieldWithIndex() {
        // given
        val generator = Exp<Person>() on Person::dogs[1]

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getExpressionNFieldWithAllIndexWithField() {
        // given
        val generator = Exp<Person>() on Person::nestedDogs["*"]["*"] on Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getExpressionFieldWithIndexWithFieldExpression1() {
        // given
        val generator = Exp<Person>() on Person::nestedDogs[1][2] on Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getExpressionFieldWithIndexWithFieldExpression2() {
        // given
        val generator = Exp<Person>() on Person::nestedDogs get 1 get 2 on Dog::name

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*].name")
    }

    @Test
    fun getExpressionFieldWithIndexWithFieldExpression3() {
        // given
        val generator = Exp<Person>()
            .on(Person::nestedDogs)[1][2]
            .on(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionFieldWithIndexWithFieldExpression4() {
        // given
        val generator = Exp<Person>()
            .on(Person::nestedDogs[1][2])
            .on(Dog::name)

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    data class Person(
        val name: String?,
        val dog: Dog,
        val dogs: List<Dog>,
        val nestedDogs: List<List<Dog>>,
    )

    data class Dog(val name: String, val loves: List<Int>)
}

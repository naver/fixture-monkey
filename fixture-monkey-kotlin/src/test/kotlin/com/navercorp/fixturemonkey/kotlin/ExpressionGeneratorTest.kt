package com.navercorp.fixturemonkey.kotlin

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ExpressionGeneratorTest {
    @Test
    fun getExpressionEmpty() {
        // given
        val generator = from(Person::class.java) { this }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("")
    }

    @Test
    fun getExpressionFieldWithImplicitRangeTo() {
        // given
        val generator = from(Person::class.java) { this..Person::dog }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog")
    }

    @Test
    fun getExpressionFieldWithExplicitProperty() {
        // given
        val generator = from(Person::class.java) { this.property(Person::dogs) }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs")
    }

    @Test
    fun getExpressionNestedField() {
        // given
        val generator = from(Person::class.java) { this..Person::dog..Dog::name }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dog.name")
    }

    @Test
    fun getExpressionListWithImplicitRangeTo() {
        // given
        val generator = from(Person::class.java) { this..Person::dogs..Index(List<Dog>::get, 1) }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getExpressionListWithImplicitIndex() {
        // given
        val generator = from(Person::class.java) { this.property(Person::dogs).method(Index(List<Dog>::get, 1)) }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[1]")
    }

    @Test
    fun getExpressionListWithImplicitAllIndex() {
        // given
        val generator = from(Person::class.java) { this.property(Person::dogs).method(AllIndex(List<Dog>::get)) }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getExpressionListAllIndex() {
        // given
        val generator = from(Person::class.java) { this..Person::dogs..AllIndex(List<Dog>::get) }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("dogs[*]")
    }

    @Test
    fun getExpressionNestedListAllIndex() {
        // given
        val generator = from(Person::class.java) {
            this..Person::nestedDogs..AllIndex(List<List<Dog>>::get)..AllIndex(List<Dog>::get)
        }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[*][*]")
    }

    @Test
    fun getExpressionNestedListIndex() {
        // given
        val generator =
            from(Person::class.java) {
                this..Person::nestedDogs..Index(List<List<Dog>>::get, 1)..Index(
                    List<Dog>::get,
                    2
                )
            }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2]")
    }

    @Test
    fun getExpressionNestedListField() {
        // given
        val generator =
            from(Person::class.java) {
                this..Person::nestedDogs..Index(List<List<Dog>>::get, 1)..Index(
                    List<Dog>::get,
                    2
                )..Dog::name
            }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].name")
    }

    @Test
    fun getExpressionNestedListIndexFieldListIndex() {
        // given
        val generator =
            from(Person::class.java) {
                this..Person::nestedDogs..Index(List<List<Dog>>::get, 1)..Index(
                    List<Dog>::get,
                    2
                )..Dog::loves..Index(List<Int>::get, 1)
            }

        // when
        val actual = generator.generate()

        then(actual).isEqualTo("nestedDogs[1][2].loves[1]")
    }

    data class Person(
        val name: String?,
        val dog: Dog,
        val dogs: List<Dog>,
        val nestedDogs: List<List<Dog>>,
    )

    data class Dog(val name: String, val loves: List<Int>)
}

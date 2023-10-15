package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.experimental.constructor
import com.navercorp.fixturemonkey.kotlin.experimental.parameter
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.instantiateBy
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.JavaConstructorTestSpecs.JavaTypeObject
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class InstantiatorTest {
    @RepeatedTest(TEST_COUNT)
    fun instantiateDslPrimaryConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> {
                constructor {
                    parameter<String>("foo")
                    parameter<Int>("bar")
                }
            }
            .sample()
            .bar

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateDslSecondaryConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> {
                constructor {
                    parameter<String>("foo")
                }
            }
            .sample()
            .bar

        then(actual).isEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateDslPrimaryConstructorWithoutParameterName() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> {
                constructor {
                    parameter<String>()
                    parameter<Int>()
                }
            }
            .sample()
            .bar

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateDslSecondaryConstructorWithoutParameterName() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> {
                constructor {
                    parameter<String>()
                }
            }
            .sample()
            .bar

        then(actual).isEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun setWithInstantiateDsl() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> {
                constructor {
                    parameter<String>()
                }
            }
            .set("foo", "test")
            .sample()
            .foo

        then(actual).isEqualTo("test")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByPrimaryConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> { constructor<String, Int>() }
            .sample()
            .bar

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateBySecondConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy<_, Foo> { constructor<String>() }
            .sample()
            .bar

        then(actual).isEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByGeneric() {
        val actual = SUT.giveMeExperimentalBuilder<Bar<String>>()
            .instantiateBy<Bar<String>, Bar<String>> { constructor<String>() }
            .sample()
            .bar

        then(actual).isNotNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObject() {
        val actual = SUT.giveMeExperimentalBuilder<JavaTypeObject>()
            .instantiateBy<JavaTypeObject, JavaTypeObject> { constructor<Int>() }
            .sample()
            .string

        then(actual).isEqualTo("second")
    }

    class Foo(val foo: String, val bar: Int) {
        constructor(foo: String) : this(foo, 1)
    }

    class Bar<T>(val bar: T)

    companion object {
        private val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.experimental.Instantiator
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.experimental.constructor
import com.navercorp.fixturemonkey.kotlin.experimental.instantiateBy
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class InstantiatorTest {
    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaPrimaryConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy {
                Instantiator.constructor<Foo>()
                    .parameter(String::class.java, "foo")
                    .parameter(Int::class.java, "bar")
            }
            .sample()
            .bar

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateDslPrimaryConstructor() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy {
                constructor<Foo> {
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
            .instantiateBy {
                constructor<Foo> {
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
            .instantiateBy {
                constructor<Foo> {
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
            .instantiateBy {
                constructor<Foo> {
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
            .instantiateBy {
                constructor<Foo> {
                    parameter<String>()
                }
            }
            .set("foo", "bar")
            .sample()
            .foo

        then(actual).isEqualTo("bar")
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

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.experimental.Instantiator
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
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
    fun instantiatePrimaryConstructor() {
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
    fun instantiateSecondaryConstructor() {
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
    fun instantiatePrimaryConstructorWithoutParameterName() {
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
    fun instantiateSecondaryConstructorWithoutParameterName() {
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
    fun setWithInstantiate() {
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

    @RepeatedTest(TEST_COUNT)
    fun instantiateRootType() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy {
                constructor {
                    parameter<String>()
                }
            }
            .sample()
            .bar

        then(actual).isEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateGenericType() {
        val actual = SUT.giveMeExperimentalBuilder<Bar<String>>()
            .instantiateBy {
                constructor<Bar<String>> {
                    parameter<String>()
                }
            }
            .sample()
            .bar

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByConstructorTwoTypes() {
        val actual = SUT.giveMeExperimentalBuilder<Baz>()
            .instantiateBy {
                constructor<Foo> {
                    parameter<String>()
                }
                constructor<Bar<String>> {
                    parameter<String>()
                }
            }
            .sample()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByMixedTwoTypes() {
        val actual = SUT.giveMeExperimentalBuilder<Baz>()
            .instantiateBy {
                constructor<Bar<String>> {
                    parameter<String>()
                }
                factory<Foo> {
                    parameter<Int>()
                }
            }
            .sample()

        then(actual).isNotNull
        then(actual.foo.foo).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByCompanionObjectFactoryMethod() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy {
                factory<Foo> {
                    parameter<Int>()
                }
            }
            .sample()
            .foo

        then(actual).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun setInstantiateByCompanionObjectFactoryMethod() {
        val actual = SUT.giveMeExperimentalBuilder<Foo>()
            .instantiateBy {
                factory<Foo> {
                    parameter<Int>()
                }
            }
            .set("bar", 1)
            .sample()
            .bar

        then(actual).isEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootProperty() {
        val actual = SUT.giveMeExperimentalBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy { property() }
            .sample()

        then(actual.string).isNotEqualTo("string")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByProperty() {
        val actual = SUT.giveMeExperimentalBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy { property<JavaConstructorTestSpecs.JavaTypeObject>() }
            .sample()

        then(actual.string).isNotEqualTo("string")
    }

    class Foo(val foo: String, val bar: Int) {
        constructor(foo: String) : this(foo, 1)

        companion object {
            fun build(bar: Int): Foo = Foo("factory", bar)
        }
    }

    class Bar<T>(val bar: T)

    class Baz(val foo: Foo, val bar: Bar<String>)

    companion object {
        private val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}

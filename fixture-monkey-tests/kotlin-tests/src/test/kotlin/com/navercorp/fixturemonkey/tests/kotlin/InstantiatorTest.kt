package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.instantiator.Instantiator
import com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import java.lang.reflect.Modifier

class InstantiatorTest {
    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaPrimaryConstructor() {
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Foo>()
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
        val actual = SUT.giveMeBuilder<Bar<String>>()
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
        val actual = SUT.giveMeBuilder<Baz>()
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
        val actual = SUT.giveMeBuilder<Baz>()
            .instantiateBy {
                constructor<Bar<String>> {
                    parameter<String>()
                }
                factory<Foo>("build") {
                    parameter<Int>()
                }
            }
            .sample()

        then(actual).isNotNull
        then(actual.foo.foo).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByCompanionObjectFactoryMethodWithoutType() {
        val actual = SUT.giveMeBuilder<Foo>()
            .instantiateBy {
                factory("build")
            }
            .sample()
            .foo

        then(actual).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByCompanionObjectFactoryMethod() {
        val actual = SUT.giveMeBuilder<Foo>()
            .instantiateBy {
                factory<Foo>("build")
            }
            .sample()
            .foo

        then(actual).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByCompanionObjectFactoryMethodWithoutTypeWithParameter() {
        val actual = SUT.giveMeBuilder<Foo>()
            .instantiateBy {
                factory("build") {
                    parameter<Int>()
                }
            }
            .sample()
            .foo

        then(actual).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateByCompanionObjectFactoryMethodWithParameter() {
        val actual = SUT.giveMeBuilder<Foo>()
            .instantiateBy {
                factory<Foo>("build") {
                    parameter<Int>()
                }
            }
            .sample()
            .foo

        then(actual).isEqualTo("factory")
    }

    @RepeatedTest(TEST_COUNT)
    fun setInstantiateByCompanionObjectFactoryMethodWithParameter() {
        val actual = SUT.giveMeBuilder<Foo>()
            .instantiateBy {
                factory<Foo>("build") {
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
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    property()
                }
            }
            .sample()
            .string

        then(actual).isNotEqualTo("string")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootPropertyFilter() {
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    property {
                        filter { !it.isFinal }
                    }
                }
            }
            .sample()
            .string

        then(actual).isEqualTo("third")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootField() {
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    javaField()
                }
            }
            .sample()
            .string

        then(actual).isNotEqualTo("string")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootFieldFilter() {
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    javaField {
                        filter { !Modifier.isFinal(it.modifiers) }
                    }
                }
            }
            .sample()
            .string

        then(actual).isEqualTo("third")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootJavaBeans() {
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    javaBeansProperty()
                }
            }
            .sample()
            .string

        then(actual).isNotEqualTo("string")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaObjectByRootFieldJavaBeans() {
        val actual = SUT.giveMeBuilder<JavaConstructorTestSpecs.JavaTypeObject>()
            .instantiateBy {
                constructor {
                    javaBeansProperty {
                        filter { "string" != it.name }
                    }
                }
            }
            .sample()
            .string

        then(actual).isEqualTo("third")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiatePropertyByKotlinProperty() {
        class PropertyObject {
            var string: String = "test"
        }

        class ConstructorObject(val propertyObject: PropertyObject)

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy { constructor<PropertyObject> { property() } }
            .sample()
            .propertyObject
            .string

        then(actual).isNotEqualTo("test")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiatePropertyByKotlinPropertyFilter() {
        class PropertyObject {
            var string: String = "test"
        }

        class ConstructorObject(val propertyObject: PropertyObject)

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy {
                constructor<PropertyObject> {
                    property {
                        filter {
                            it.name != "string"
                        }
                    }
                }
            }
            .sample()
            .propertyObject
            .string

        then(actual).isEqualTo("test")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiatePropertyByKotlinPropertyPrivateSetter() {
        class PropertyObject {
            var string: String = "test"
                private set
        }

        class ConstructorObject(val propertyObject: PropertyObject)

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy { constructor<PropertyObject> { property() } }
            .sample()
            .propertyObject
            .string

        then(actual).isEqualTo("test")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateNoArgsConstructor() {
        class NoArgsConstructorObject() {
            var string: String = "noArgs"

            constructor(string: String) : this() {
                this.string = string
            }
        }

        val actual = SUT.giveMeBuilder<NoArgsConstructorObject>()
            .instantiateBy { constructor() }
            .sample()
            .string

        then(actual).isEqualTo("noArgs")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiatePropertyNoArgsConstructor() {
        class NoArgsConstructorObject {
            var string: String = "noArgs"
        }

        class AllArgsConstructorObject(val value: NoArgsConstructorObject)

        val actual = SUT.giveMeBuilder<AllArgsConstructorObject>()
            .instantiateBy { constructor<NoArgsConstructorObject>() }
            .sample()
            .value
            .string

        then(actual).isEqualTo("noArgs")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateCompositeJavaObjectByProperty() {
        class ConstructorAndProperty(val value: String) {
            var propertyNotInConstructor: String? = null
        }

        val actual = SUT.giveMeBuilder<ConstructorAndProperty>()
            .instantiateBy {
                constructor {
                    parameter<String>()
                    property()
                }
            }
            .setNotNull("propertyNotInConstructor")
            .sample()

        then(actual.value).isNotNull()
        then(actual.propertyNotInConstructor).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiatePrivateConstructor() {
        class PrivateConstructorObject private constructor(val value: String)

        val actual = SUT.giveMeBuilder<PrivateConstructorObject>()
            .instantiateBy {
                constructor()
            }
            .sample()
            .value

        then(actual).isNotNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateDefaultArgumentConstructor() {
        class ConstructorObject(val value: String = "default")

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy { constructor() }
            .sample()
            .value

        then(actual).isNotEqualTo("default")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateJavaConstructorWithDefaultArgument() {
        class ConstructorObject(val value: String = "default")

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy{
                Instantiator.constructor<ConstructorObject>()
            }
            .sample()
            .value

        then(actual).isNotEqualTo("default")
    }

    @RepeatedTest(TEST_COUNT)
    fun instantiateConstructorWithDefaultArgument() {
        class ConstructorObject(val value: String = "default")

        val actual = SUT.giveMeBuilder<ConstructorObject>()
            .instantiateBy {
                constructor {
                    parameter<String>(useDefaultArgument = true)
                }
            }
            .sample()
            .value

        then(actual).isEqualTo("default")
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

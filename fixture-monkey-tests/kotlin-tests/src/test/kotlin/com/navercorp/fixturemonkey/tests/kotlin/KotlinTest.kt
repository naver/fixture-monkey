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

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot
import com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString
import com.navercorp.fixturemonkey.api.introspector.AnonymousArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.api.type.Types.GeneratingWildcardType
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.expression.root
import com.navercorp.fixturemonkey.kotlin.get
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.intoGetter
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.maxSize
import com.navercorp.fixturemonkey.kotlin.minSize
import com.navercorp.fixturemonkey.kotlin.pushExactTypeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.register
import com.navercorp.fixturemonkey.kotlin.set
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.kotlin.setLazy
import com.navercorp.fixturemonkey.kotlin.setNotNull
import com.navercorp.fixturemonkey.kotlin.setNull
import com.navercorp.fixturemonkey.kotlin.setPostCondition
import com.navercorp.fixturemonkey.kotlin.size
import com.navercorp.fixturemonkey.kotlin.sizeExp
import com.navercorp.fixturemonkey.kotlin.sizeExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.BuilderJavaTestSpecs.BuilderObjectCustomBuildName
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.ArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.ImmutableJavaTestSpecs.NestedArrayObject
import com.navercorp.fixturemonkey.tests.kotlin.JavaConstructorTestSpecs.JavaTypeObject
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.LinkedList
import java.util.TreeSet
import java.util.UUID
import kotlin.reflect.jvm.javaMethod

class KotlinTest {
    @Test
    fun kotlinObjectUseJavaGetterThrows() {
        class KotlinObject(val value: String)

        thenThrownBy {
            SUT.giveMeBuilder<KotlinObject>()
                .set(javaGetter(KotlinObject::value), "test")
                .sample()
                .value
        }.cause()
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Kotlin type could not resolve property name.")
    }

    @Test
    fun javaGetter() {
        val expected = "test"

        val actual = SUT.giveMeExperimentalBuilder<JavaTypeObject>()
            .instantiateBy {
                constructor {
                    parameter<String>("string")
                    parameter<Int>()
                }
            }
            .set(javaGetter(JavaTypeObject::getString), expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpArrayElement() {
        // given
        class ArrayObject(val array: Array<String>)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<ArrayObject>()
            .sizeExp(ArrayObject::array, 1)
            .setExp(ArrayObject::array[0], expected)
            .sample()
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpNestedArrayElement() {
        // given
        class ArrayObject(val array: Array<String>)
        class NestedArrayObject(val obj: ArrayObject)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<NestedArrayObject>()
            .sizeExp(NestedArrayObject::obj into ArrayObject::array, 1)
            .setExp(NestedArrayObject::obj into ArrayObject::array[0], expected)
            .sample()
            .obj
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpGetterArrayElement() {
        // given
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<ArrayObject>()
            .instantiateBy {
                constructor<ArrayObject> {
                    parameter<Array<String>>(parameterName = "array")
                }
            }
            .sizeExpGetter(ArrayObject::getArray, 1)
            .setExpGetter(ArrayObject::getArray[0], expected)
            .sample()
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpGetterNestedArrayElement() {
        // given
        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<NestedArrayObject>()
            .instantiateBy {
                constructor<NestedArrayObject> {
                    parameter<ArrayObject>(parameterName = "obj")
                }
                constructor<ArrayObject> {
                    parameter<Array<String>>(parameterName = "array")
                }
            }
            .sizeExp(NestedArrayObject::getObj intoGetter ArrayObject::getArray, 1)
            .setExp(NestedArrayObject::getObj intoGetter ArrayObject::getArray[0], expected)
            .sample()
            .obj
            .array[0]

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun constructorArbitraryIntrospectorWithoutPrimaryConstructor() {
        // given
        class ConstructorWithoutAnyAnnotations(val string: String)

        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build()

        // when
        val actual: ConstructorWithoutAnyAnnotations = sut.giveMeOne()

        // then
        then(actual).isNull()
    }

    @Test
    fun failoverIntrospectorHandlingExceptionWhenDeclaring() {
        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<UUID>(
                FailoverIntrospector(
                    listOf(
                        ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                        FactoryMethodArbitraryIntrospector(
                            FactoryMethodArbitraryIntrospector.FactoryMethodWithParameterNames(
                                UUID::randomUUID.javaMethod,
                                listOf(),
                            ),
                        ),
                    ),
                ),
            )
            .build()

        val actual = sut.giveMeOne<UUID>()

        then(actual).isNotNull
    }

    @Test
    fun setChild() {
        open class Parent(val parent: String)
        class Child : Parent("parent")

        val actual = SUT.giveMeBuilder<Parent>()
            .set(Child())
            .sample()
            .parent

        then(actual).isEqualTo("parent")
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertySet() {
        val actual = SUT.giveMeExperimentalBuilder<String>()
            .customizeProperty(typedRoot<String>()) {
                it.filter { str -> str.length > 5 }
                    .map { str -> str.substring(0..3) }
            }
            .sample()

        then(actual).hasSizeLessThanOrEqualTo(4)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyFilter() {
        val now = Instant.now()
        val min = now.minus(365, ChronoUnit.DAYS)
        val max = now.plus(365, ChronoUnit.DAYS)

        val actual = SUT.giveMeExperimentalBuilder<Instant>()
            .customizeProperty(typedRoot<Instant>()) {
                it.filter { instant -> instant.isAfter(min) && instant.isBefore(max) }
            }
            .sample()

        then(actual).isBetween(min, max)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleCustomizedWildcardType() {
        // given
        class MapObject(val map: Map<String, *>)

        val expected = "test"

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .pushExactTypeArbitraryIntrospector<GeneratingWildcardType> {
                ArbitraryIntrospectorResult(
                    CombinableArbitrary.from(expected)
                )
            }
            .build()

        // when
        val actual = sut.giveMeBuilder<MapObject>()
            .sizeExp(MapObject::map, 1)
            .sample()
            .map
            .values
            .first()

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun overwriteExistingType() {
        val expected = "string"
        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<String> {
                ArbitraryIntrospectorResult(
                    CombinableArbitrary.from(expected)
                )
            }
            .build()

        val actual: String = sut.giveMeOne()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setToObject() {
        // given
        class Object(val obj: Any?)

        val expected = "test"

        // when
        val actual = SUT.giveMeBuilder<Object>()
            .setExp(Object::obj, expected)
            .sample()
            .obj

        // then
        then(actual).isEqualTo(expected)
    }

    @Test
    fun primaryConstructorArbitraryIntrospectorNotThrows() {
        val actual: Timestamp = SUT.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun beanArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun fieldReflectionArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun constructorPropertiesArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun anonymousArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(AnonymousArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun builderArbitraryIntrospectorNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build()

        val actual: Timestamp = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun builderArbitraryIntrospectorMissBuildMethodNotThrows() {
        val sut = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build()

        val actual: BuilderObjectCustomBuildName = sut.giveMeOne()

        then(actual).isNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun setLazyJustNotChanged() {
        // given
        class StringObject(val string: String)

        val expected = StringObject("test")

        // when
        val actual = SUT.giveMeBuilder<StringObject>()
            .setLazy("$") { Values.just(expected) }
            .setExp(StringObject::string, "notTest")
            .sample()

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun pushPrimaryConstructorIntrospector() {
        // given
        class StringObject(val string: String)

        val sut = FixtureMonkey.builder()
            .pushExactTypeArbitraryIntrospector<StringObject>(PrimaryConstructorArbitraryIntrospector.INSTANCE)
            .build()

        // when
        val actual = sut.giveMeOne<StringObject>().string

        // then
        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setConcreteTypeChildReturnsExactlyConcreteTypeChildType() {
        // given
        abstract class AbstractClass
        open class ConcreteType(val parentValue: String) : AbstractClass()
        class ConcreteTypeChild(val childValue: String) : ConcreteType("parent")

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AbstractClass::class.java,
                        listOf(ConcreteType::class.java, ConcreteTypeChild::class.java)
                    )
            )
            .build()

        // when
        val actual = sut.giveMeBuilder<AbstractClass>()
            .set("$", ConcreteTypeChild(""))
            .sample()

        then(actual).isExactlyInstanceOf(ConcreteTypeChild::class.java)
    }

    @Test
    fun setRootExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<String>()
            .set(String::root, expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setLazyRootExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<String>()
            .setLazy(String::root) { expected }
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setNullRootExp() {
        val actual = SUT.giveMeBuilder<String?>()
            .setNull(String::root)
            .sample()

        then(actual).isNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun setNotNullRootExp() {
        val actual = SUT.giveMeBuilder<String?>()
            .setNotNull(String::root)
            .sample()

        then(actual).isNotNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun sizeRootExp() {
        val actual = SUT.giveMeBuilder<List<String>>()
            .size(List<String>::root, 1)
            .sample()

        then(actual).hasSize(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun sizeRangeRootExp() {
        val actual = SUT.giveMeBuilder<List<String>>()
            .size(List<String>::root, 1, 3)
            .sample()

        then(actual).hasSizeBetween(1, 3)
    }

    @RepeatedTest(TEST_COUNT)
    fun minSizeRootExp() {
        val actual = SUT.giveMeBuilder<List<String>>()
            .minSize(List<String>::root, 1)
            .sample()

        then(actual).hasSizeGreaterThanOrEqualTo(1)
    }

    @RepeatedTest(TEST_COUNT)
    fun maxSizeRootExp() {
        val actual = SUT.giveMeBuilder<List<String>>()
            .maxSize(List<String>::root, 1)
            .sample()

        then(actual).hasSizeLessThanOrEqualTo(1)
    }

    @Test
    fun setPostConditionRootExp() {
        val actual = SUT.giveMeBuilder<String>()
            .setPostCondition(String::root) { it.length < 5 }
            .sample()

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<List<String>>()
            .size(List<String>::root, 1)
            .set(List<String>::root[0], expected)
            .sample()

        then(actual[0]).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootArrayElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Array<String>>()
            .size(Array<String>::root, 1)
            .set(Array<String>::root[0], expected)
            .sample()

        then(actual[0]).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<List<String>>()
            .size(List<String>::root, 3)
            .set(List<String>::root["*"], expected)
            .sample()

        then(actual).allMatch { it == expected }
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootArrayAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Array<String>>()
            .size(Array<String>::root, 3)
            .set(Array<String>::root["*"], expected)
            .sample()

        then(actual).allMatch { it == expected }
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootNestedListElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<List<List<String>>>()
            .size(List<List<String>>::root, 1)
            .size(List<List<String>>::root[0], 1)
            .set(List<List<String>>::root[0][0], expected)
            .sample()[0][0]

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootNestedListAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<List<List<String>>>()
            .size(List<List<String>>::root, 3)
            .size(List<List<String>>::root["*"], 3)
            .set(List<List<String>>::root["*"]["*"], expected)
            .sample()

        then(actual).allMatch { list -> list.all { it == expected } }
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootNestedArrayElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Array<Array<String>>>()
            .size(Array<Array<String>>::root, 1)
            .size(Array<Array<String>>::root[0], 1)
            .set(Array<Array<String>>::root[0][0], expected)
            .sample()[0][0]

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun setRootNestedArrayAllElementExp() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Array<Array<String>>>()
            .size(Array<Array<String>>::root, 3)
            .size(Array<Array<String>>::root["*"], 3)
            .set(Array<Array<String>>::root["*"]["*"], expected)
            .sample()
            .flatten()

        then(actual).allMatch { it == expected }
    }

    @Test
    fun setRootPropertyExp() {
        val expected = "expected"

        class StringObject(val string: String)

        val actual = SUT.giveMeBuilder<StringObject>()
            .set(StringObject::root into StringObject::string, expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @Test
    fun registerAssignableType() {
        // given
        open class Parent(val parent: String)

        class Child(parent: String) : Parent(parent)

        val expected = "registered"
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .register {
                it.giveMeBuilder<Parent>()
                    .setExp(Parent::parent, expected)
            }
            .build()

        // when
        val actual = sut.giveMeOne<Child>().parent

        // then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun propertyCandidateResolverReturnsConcreteListType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            List::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(LinkedList::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: List<String> = sut.giveMeOne()

        then(actual).isInstanceOf(LinkedList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun nestedPropertyCandidateResolverReturnsConcreteListType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Collection::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(List::class.java))
                        ),
                        MatcherOperator.exactTypeMatchOperator(
                            List::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(LinkedList::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(LinkedList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun propertyCandidateResolverReturnsConcreteSetType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Set::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(TreeSet::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Set<String> = sut.giveMeOne()

        then(actual).isInstanceOf(TreeSet::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun nestedPropertyCandidateResolverReturnsConcreteSetType() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin {
                it.candidateConcretePropertyResolvers(
                    listOf(
                        MatcherOperator.exactTypeMatchOperator(
                            Collection::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(Set::class.java))
                        ),
                        MatcherOperator.exactTypeMatchOperator(
                            Set::class.java,
                            ConcreteTypeCandidateConcretePropertyResolver(listOf(TreeSet::class.java))
                        )
                    )
                )
            }
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(TreeSet::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun interfaceImplementsExtendsInterface() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .interfaceImplements(Collection::class.java, listOf(Set::class.java))
            )
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(HashSet::class.java)
    }

    @Test
    fun kotlinLambda() {
        // given
        class KotlinLambdaValue(val lambda: (String, String, String) -> Unit)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda).isNotNull
    }

    @Test
    fun kotlinLambdaOnlyReturnType() {
        // given
        class KotlinLambdaValue(val lambda: () -> String)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda.invoke()).isNotNull
    }

    @Test
    fun decomposeKotlinLambdaOnlyReturnType() {
        // given
        class KotlinLambdaValue(val lambda: () -> String)

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .thenApply { _, _ -> }
            .sample()

        then(actual.lambda.invoke()).isNotNull
    }

    @Test
    fun kotlinLambdaReturnType() {
        // given
        class KotlinLambdaValue(val lambda: (String) -> String)

        // when
        val actual: KotlinLambdaValue = SUT.giveMeOne()

        then(actual.lambda.invoke("test")).isNotNull
    }

    @Test
    fun decomposeKotlinLambdaReturnType() {
        // given
        class KotlinLambdaValue(val lambda: (String) -> String)

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .thenApply { _, _ -> }
            .sample()

        then(actual.lambda.invoke("test")).isNotNull
    }

    @Test
    fun setJustKotlinLambda() {
        // given
        class KotlinLambdaValue(val lambda: (String, String, String) -> Unit)

        val expected: (String, String, String) -> Unit = { _, _, _ -> }

        // when
        val actual = SUT.giveMeBuilder<KotlinLambdaValue>()
            .setExp(KotlinLambdaValue::lambda, Values.just(expected))
            .sample()

        then(actual.lambda).isNotNull
    }

    @Test
    fun interfaceImplementsAssignableTypeGeneratesConcreteTypeNotThrows() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .interfaceImplements(
                        AssignableTypeMatcher(Collection::class.java),
                        listOf(LinkedList::class.java)
                    )

            )
            .build()

        val actual: ArrayList<String> = sut.giveMeOne()

        then(actual).isInstanceOf(ArrayList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun circularReferenceDefaultArgument() {
        val actual = SUT.giveMeOne<CircularReferenceDefaultArgument>().value

        then(actual).isNotNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun circularReferenceNullable() {
        val actual: CircularReferenceValueNullable = SUT.giveMeOne()

        then(actual).isNotNull()
    }

    @Test
    fun complexAbstractExtendsReturnsFirst() {
        abstract class ParentAbstractClass

        abstract class FirstAbstractClass : ParentAbstractClass()
        abstract class SecondAbstractClass : ParentAbstractClass()

        class FirstConcreteClass : FirstAbstractClass()
        class SecondConcreteClass : SecondAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AssignableTypeMatcher(ParentAbstractClass::class.java)
                    ) { property ->
                        when (property.type) {
                            FirstAbstractClass::class.java -> listOf(PropertyUtils.toProperty(FirstConcreteClass::class.java))
                            SecondAbstractClass::class.java -> listOf(PropertyUtils.toProperty(SecondConcreteClass::class.java))
                            else -> throw NotImplementedError()
                        }
                    }

            )
            .build()

        val actual: FirstAbstractClass = sut.giveMeOne()

        then(actual).isInstanceOf(FirstConcreteClass::class.java)
    }

    @Test
    fun complexAbstractExtendsReturnsSecond() {
        abstract class ParentAbstractClass

        abstract class FirstAbstractClass : ParentAbstractClass()
        abstract class SecondAbstractClass : ParentAbstractClass()

        class FirstConcreteClass : FirstAbstractClass()
        class SecondConcreteClass : SecondAbstractClass()

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .abstractClassExtends(
                        AssignableTypeMatcher(ParentAbstractClass::class.java)
                    ) { property ->
                        when (property.type) {
                            FirstAbstractClass::class.java -> listOf(PropertyUtils.toProperty(FirstConcreteClass::class.java))
                            SecondAbstractClass::class.java -> listOf(PropertyUtils.toProperty(SecondConcreteClass::class.java))
                            else -> throw NotImplementedError()
                        }
                    }

            )
            .build()

        val actual: SecondAbstractClass = sut.giveMeOne()

        then(actual).isInstanceOf(SecondConcreteClass::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostCondition() {
        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .setPostCondition<String>("string") { it.length < 5 }
            .sample()
            .string

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostConditionWithProperty() {
        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .setPostCondition<String>(StringObject::string) { it.length < 5 }
            .sample()
            .string

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyAfterSet() {
        // given
        class StringValue(val value: String)

        val expected = "abc"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringValue>()
            .setExp(StringValue::value, "abcdef")
            .customizeProperty(typedString<String>("value")) {
                it.map { str -> str.substring(0..2) }
            }
            .sample()
            .value

        //then
        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun customizePropertyIgnoredIfSet() {
        // given
        class StringValue(val value: String)

        val expected = "fixed"

        // when
        val actual = SUT.giveMeKotlinBuilder<StringValue>()
            .customizeProperty(typedString<String>("value")) {
                it.filter { value -> value.length > 5 }
            }
            .setExp(StringValue::value, expected)
            .sample()
            .value

        //then
        then(actual).isEqualTo(expected)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}

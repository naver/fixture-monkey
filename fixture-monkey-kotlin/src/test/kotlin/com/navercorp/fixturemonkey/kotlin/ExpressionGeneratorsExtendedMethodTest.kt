package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.LabMonkey
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.spec.JavaObject
import net.jqwik.api.Arbitraries
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import java.util.function.Consumer

class ExpressionGeneratorsExtendedMethodTest {
    private val sut: LabMonkey = LabMonkey.labMonkeyBuilder()
        .plugin(KotlinPlugin())
        .build()

    @Property
    fun setInnerExp() {
        // when
        val actual = sut.giveMeBuilder<Person>()
            .setInnerExp(Person::mapDog) { m -> m.minSize(1).entry("dog", "dog") }
            .sample()
            .mapDog

        then(actual.get("dog")).isEqualTo("dog")
    }

    @Property
    fun setLazyExp() {
        // when
        val builder = sut.giveMeBuilder<Person>()
            .setLazyExp(Person::name) { Arbitraries.strings().ofLength(3)}

        val actual1 = builder.sample().name
        val actual2 = builder.sample().name

        then(actual1)
        then(actual2)
    }



}

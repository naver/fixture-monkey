package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.LabMonkey
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.spec.JavaObject
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenNoException

class FixtureMonkeyV04JavaTest {
    private val sut: LabMonkey = LabMonkey.labMonkeyBuilder()
        .plugin(KotlinPlugin())
        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
        .build()

    @Property
    fun sampleJavaObject() {
        // when
        val actual = sut.giveMeOne<JavaObject>()

        then(actual).isNotNull
    }

    @Property
    fun fixedJavaObject() {
        thenNoException()
            .isThrownBy {
                sut.giveMeBuilder<JavaObject>()
                    .fixed()
                    .sample()
            }
    }
}

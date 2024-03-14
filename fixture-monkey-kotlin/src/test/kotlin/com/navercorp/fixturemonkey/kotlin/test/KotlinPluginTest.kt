package com.navercorp.fixturemonkey.kotlin.test

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test

/**
 * Kotlin plugin test
 *
 * test for behavior of default introspector using failover introspector
 *
 * - default introspector is PrimaryConstructorArbitraryIntrospector
 * - if it fails, it uses BeanArbitraryIntrospector
 *
 * this action is for supporting instantiating java class
 */
class KotlinPluginTest {
    private val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    @Test
    fun kotlinClassWithJavaClass() {

        val sut: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
        // when
        val actual = sut.giveMeOne<KotlinClassWithJavaClass>()

        BDDAssertions.then(actual).isNotNull
        BDDAssertions.then(actual.javaObject).isNotNull
    }

    @Test
    fun sampleMapValue() {
        // when
        val actual = sut.giveMeOne<MapValue>()

        BDDAssertions.then(actual).isNotNull
        BDDAssertions.then(actual.map).isNotNull
    }
}

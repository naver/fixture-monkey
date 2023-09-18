package com.navercorp.fixturemonkey.tests.concurrent.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class ConcurrentTest {
    @RepeatedTest(TestEnvironment.TEST_COUNT)
    fun test1() {
        val actual: KotlinObject = SUT.giveMeOne()
        then(actual).isNotNull
    }

    @RepeatedTest(TestEnvironment.TEST_COUNT)
    fun test2() {
        val actual: KotlinObject = SUT.giveMeOne()
        then(actual).isNotNull
    }

    @RepeatedTest(TestEnvironment.TEST_COUNT)
    fun test3() {
        val actual: KotlinObject = SUT.giveMeOne()
        then(actual).isNotNull
    }

    data class KotlinObject(val value: String, val map: Map<String, String>)

    companion object {
        private val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}

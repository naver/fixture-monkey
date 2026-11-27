package com.navercorp.fixturemonkey.tests.memory.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenCode
import org.junit.jupiter.api.Test

class LargeSetValueMemoryTest {
    @Test
    fun heapIsLimitedTo512Mb() {
        // when
        val actual = Runtime.getRuntime().maxMemory()

        // then
        then(actual).isLessThanOrEqualTo(MAX_HEAP_BYTES)
    }

    @Test
    fun setLargeArrayFitsInLimitedHeap() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
        val data = ByteArray(LARGE_ARRAY_SIZE)

        // when, then
        thenCode { sut.giveMeBuilder<Payload>().set("data", data).sample() }
            .doesNotThrowAnyException()
    }

    @Test
    fun setLargeArrayInStrictModeFitsInLimitedHeap() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .useExpressionStrictMode()
            .build()
        val data = ByteArray(LARGE_ARRAY_SIZE)

        // when, then
        thenCode { sut.giveMeBuilder<Payload>().set("data", data).sample() }
            .doesNotThrowAnyException()
    }

    class Payload(val data: ByteArray)

    companion object {
        private const val MAX_HEAP_BYTES = 512L * 1024 * 1024
        private const val LARGE_ARRAY_SIZE = 256_000
    }
}

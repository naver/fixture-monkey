package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.OrderSheet
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.type.TypeCache
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

private const val COUNT = 500

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class KotlinObjectGenerationBenchMark {
    @Setup(value = Level.Iteration)
    fun setUp() {
        TypeCache.clearCache()
    }

    @Benchmark
    fun beanGenerateOrderSheetWithFixtureMonkey(blackhole: Blackhole) {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
            .build()
        blackhole.consume(generateOrderSheet(fixtureMonkey))
    }

    private fun generateOrderSheet(fixtureMonkey: FixtureMonkey): List<OrderSheet> =
        List(COUNT) { fixtureMonkey.giveMeOne(OrderSheet::class.java) }
}

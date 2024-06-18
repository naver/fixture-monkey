package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.OrderSheet
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.api.type.TypeCache
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinAndJavaCompositeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

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
    fun beanGenerateJavaOrderSheetWithFixtureMonkey(blackhole: Blackhole) {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
            .build()
        blackhole.consume(generateJavaOrderSheet(fixtureMonkey))
    }

    private fun generateJavaOrderSheet(fixtureMonkey: FixtureMonkey): List<OrderSheet> =
        List(COUNT) { fixtureMonkey.giveMeOne(OrderSheet::class.java) }

    @Benchmark
    fun primaryConstructorGenerateKotlinOrderSheetWithFixtureMonkey(blackhole: Blackhole) {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .objectIntrospector(PrimaryConstructorArbitraryIntrospector.INSTANCE)
            .build()
        blackhole.consume(generateKotlinOrderSheet(fixtureMonkey))
    }

    @Benchmark
    fun primaryConstructorJavaFallbackGenerateKotlinOrderSheetWithFixtureMonkey(blackhole: Blackhole) {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .build()
        blackhole.consume(generateKotlinOrderSheet(fixtureMonkey))
    }

    private fun generateKotlinOrderSheet(fixtureMonkey: FixtureMonkey): List<KotlinOrderSheet> =
        List(COUNT) { fixtureMonkey.giveMeOne(KotlinOrderSheet::class.java) }

    @Benchmark
    fun kotlinJavaCompositeGenerateOrderSheetWithFixtureMonkey(blackhole: Blackhole) {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
            .build()
        blackhole.consume(generateKotlinJavaCompositeOrderSheet(fixtureMonkey))
    }

    private fun generateKotlinJavaCompositeOrderSheet(fixtureMonkey: FixtureMonkey): List<KotlinJavaCompositeOrderSheet> =
        List(COUNT) { fixtureMonkey.giveMeOne(KotlinJavaCompositeOrderSheet::class.java) }
}

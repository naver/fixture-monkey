package com.navercorp.fixturemonkey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

@SuppressWarnings("unused")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ManipulationBenchmark {
	private static final int COUNT = 500;
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JavaxValidationPlugin())
		.build();

	@Setup(value = Level.Iteration)
	public void setUp() {
		TypeCache.clearCache();
	}

	@Benchmark
	public void apply(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheet(fixture -> fixture.giveMeBuilder(OrderSheet.class)
			.apply(((orderSheet, orderSheetArbitraryBuilder) -> {
			}))
			.sample()));
	}

	@Benchmark
	public void fixed(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheet(fixture -> fixture.giveMeBuilder(OrderSheet.class).fixed().sample()));
	}

	private List<OrderSheet> generateOrderSheet(
		Function<FixtureMonkey, OrderSheet> manipulation
	) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(manipulation.apply(SUT));
		}
		return result;
	}
}

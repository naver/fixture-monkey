package com.navercorp.fixturemonkey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

@SuppressWarnings("unused")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AdapterBenchmark {

	private static final int COUNT = 200;

	// Reused instances for warm benchmarks
	private FixtureMonkey reusedWithoutAdapter;
	private FixtureMonkey reusedWithAdapter;
	private FixtureMonkey reusedWithNoOpTracer;

	@Setup(value = Level.Trial)
	public void setUpTrial() {
		reusedWithoutAdapter = FixtureMonkey.builder().plugin(new JavaxValidationPlugin()).build();
		reusedWithAdapter = FixtureMonkey.builder()
			.plugin(new JavaxValidationPlugin())
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		reusedWithNoOpTracer = FixtureMonkey.builder()
			.plugin(new JavaxValidationPlugin())
			.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.noOp()))
			.build();
	}

	@Setup(value = Level.Trial)
	public void setUp() {
		TypeCache.clearCache();
	}

	// === Cold benchmarks (new FixtureMonkey each time) ===

	@Benchmark
	public void coldWithoutAdapter(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder().plugin(new JavaxValidationPlugin()).build();
		blackhole.consume(generateOrderSheet(fixtureMonkey));
	}

	@Benchmark
	public void coldWithAdapter(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.plugin(new JavaxValidationPlugin())
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		blackhole.consume(generateOrderSheet(fixtureMonkey));
	}

	// === Warm benchmarks (reused FixtureMonkey - typical usage) ===

	@Benchmark
	public void warmWithoutAdapter(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheet(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapter(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheet(reusedWithAdapter));
	}

	@Benchmark
	public void warmWithAdapterNoOpTracer(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheet(reusedWithNoOpTracer));
	}

	@Benchmark
	public void warmWithoutAdapterWithSet(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithSet(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapterWithSet(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithSet(reusedWithAdapter));
	}

	// === ThenApply benchmarks ===

	@Benchmark
	public void warmWithoutAdapterWithThenApply(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithThenApply(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapterWithThenApply(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithThenApply(reusedWithAdapter));
	}

	// === Nested set benchmarks ===

	@Benchmark
	public void warmWithoutAdapterWithNestedSet(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithNestedSet(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapterWithNestedSet(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithNestedSet(reusedWithAdapter));
	}

	// === Size + setNull benchmarks ===

	@Benchmark
	public void warmWithoutAdapterWithSizeAndNull(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithSizeAndNull(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapterWithSizeAndNull(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithSizeAndNull(reusedWithAdapter));
	}

	// === Timing trace benchmark (for profiling) ===

	@Benchmark
	public void warmWithAdapterWithSetTiming(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetWithSetAndTiming(reusedWithAdapter));
	}

	// === Pure sample benchmarks (no manipulation) ===

	@Benchmark
	public void warmWithoutAdapterPureSample(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetPureSample(reusedWithoutAdapter));
	}

	@Benchmark
	public void warmWithAdapterPureSample(Blackhole blackhole) throws Exception {
		blackhole.consume(generateOrderSheetPureSample(reusedWithAdapter));
	}

	private List<OrderSheet> generateOrderSheetPureSample(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(fixtureMonkey.giveMeOne(OrderSheet.class));
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheet(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(fixtureMonkey.giveMeOne(OrderSheet.class));
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheetWithSet(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(
				fixtureMonkey.giveMeBuilder(OrderSheet.class).set("id", "test-id-" + i).set("userNo", (long) i).sample()
			);
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheetWithThenApply(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(
				fixtureMonkey
					.giveMeBuilder(OrderSheet.class)
					.set("id", "base-" + i)
					.thenApply((obj, builder) -> builder.set("id", obj.getId() + "-applied"))
					.sample()
			);
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheetWithNestedSet(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(
				fixtureMonkey
					.giveMeBuilder(OrderSheet.class)
					.set("orderProducts[0].productName", "product-" + i)
					.set("orderProducts[0].quantity", i)
					.sample()
			);
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheetWithSizeAndNull(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(
				fixtureMonkey
					.giveMeBuilder(OrderSheet.class)
					.size("orderProducts", 2)
					.setNull("orderProducts[0].productName")
					.sample()
			);
		}
		return result;
	}

	private List<OrderSheet> generateOrderSheetWithSetAndTiming(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(
				fixtureMonkey.giveMeBuilder(OrderSheet.class).set("id", "test-id-" + i).set("userNo", (long) i).sample()
			);
		}
		return result;
	}
}

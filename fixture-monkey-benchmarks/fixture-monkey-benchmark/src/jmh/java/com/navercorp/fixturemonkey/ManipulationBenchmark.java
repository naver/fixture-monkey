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

import lombok.Getter;
import lombok.Setter;

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
	public void thenApply(Blackhole blackhole) throws Exception {
		blackhole.consume(generateObject(fixture -> fixture.giveMeBuilder(OrderSheet.class)
			.thenApply(((orderSheet, orderSheetArbitraryBuilder) -> {
			}))
			.sample()));
	}

	@Benchmark
	public void fixed(Blackhole blackhole) throws Exception {
		blackhole.consume(generateObject(fixture -> fixture.giveMeBuilder(OrderSheet.class).fixed().sample()));
	}

	@Benchmark
	public void setValuePostCondition(Blackhole blackhole) throws Exception {
		blackhole.consume(generateObject(fixture -> fixture.giveMeBuilder(SetValuePostConditionObject.class)
			.setPostCondition(object -> object.getValue() > 0)
			.sample()));
	}

	@Benchmark
	public void setNodePostCondition(Blackhole blackhole) throws Exception {
		blackhole.consume(generateObject(fixture -> fixture.giveMeBuilder(SetNodePostConditionObject.class)
			.setPostCondition(object -> object.getValue().size() == 2)
			.sample()));
	}

	private <T> List<T> generateObject(
		Function<FixtureMonkey, T> manipulation
	) {
		List<T> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(manipulation.apply(SUT));
		}
		return result;
	}

	@Setter
	@Getter
	public static class SetValuePostConditionObject {
		private int value;
	}

	@Setter
	@Getter
	public static class SetNodePostConditionObject {
		private List<Integer> value;
	}
}

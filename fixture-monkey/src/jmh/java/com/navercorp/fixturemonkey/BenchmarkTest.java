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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.jackson.generator.JacksonArbitraryGenerator;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

@SuppressWarnings("unused")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkTest {
	private static final int COUNT = 500;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.registerModules(new Jdk8Module(), new JavaTimeModule());

	@Setup(value = Level.Iteration)
	public void setUp() {
		PropertyCache.clearCache();
	}

	@Benchmark
	public void beanGenerateOrderSheetWithLabMonkey(Blackhole blackhole) throws Exception {
		LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
			.plugin(new JavaxValidationPlugin())
			.build();
		blackhole.consume(generateOrderSheet(labMonkey));
	}

	@Benchmark
	public void beanGenerateOrderSheetWithFixtureMonkey(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.create();
		blackhole.consume(generateOrderSheet(fixtureMonkey));
	}

	@Benchmark
	public void fieldReflectionGenerateOrderSheetWithLabMonkey(Blackhole blackhole) throws Exception {
		LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaxValidationPlugin())
			.build();
		blackhole.consume(generateOrderSheet(labMonkey));
	}

	@Benchmark
	public void fieldReflectionGenerateOrderSheetWithFixtureMonkey(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();
		blackhole.consume(generateOrderSheet(fixtureMonkey));
	}

	@Benchmark
	public void jacksonGenerateOrderSheetWithLabMonkey(Blackhole blackhole) throws Exception {
		LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
			.plugin(new JacksonPlugin())
			.plugin(new JavaxValidationPlugin())
			.build();
		blackhole.consume(generateOrderSheet(labMonkey));
	}

	@Benchmark
	public void jacksonGenerateOrderSheetWithFixtureMonkey(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.defaultGenerator(new JacksonArbitraryGenerator(OBJECT_MAPPER))
			.build();
		blackhole.consume(generateOrderSheet(fixtureMonkey));
	}

	@Benchmark
	public void builderGenerateOrderSheetWithLabMonkey(Blackhole blackhole) throws Exception {
		LabMonkey labMonkey = LabMonkey.labMonkeyBuilder()
			.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			.plugin(new JavaxValidationPlugin())
			.build();
		blackhole.consume(generateBuilderOrderSheet(labMonkey));
	}

	@Benchmark
	public void builderGenerateOrderSheetWithFixtureMonkey(Blackhole blackhole) throws Exception {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();
		blackhole.consume(generateBuilderOrderSheet(fixtureMonkey));
	}

	private List<OrderSheet> generateOrderSheet(FixtureMonkey fixtureMonkey) {
		List<OrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(fixtureMonkey.giveMeOne(OrderSheet.class));
		}
		return result;
	}

	private List<BuilderOrderSheet> generateBuilderOrderSheet(FixtureMonkey fixtureMonkey) {
		List<BuilderOrderSheet> result = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			result.add(fixtureMonkey.giveMeOne(BuilderOrderSheet.class));
		}
		return result;
	}
}

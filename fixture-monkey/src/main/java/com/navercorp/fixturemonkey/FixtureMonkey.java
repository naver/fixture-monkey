package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.engine.JqwikProperties;
import net.jqwik.engine.SourceOfRandomness;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryBuilder;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryGenerator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.arbitrary.CompositeArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.arbitrary.PrimitiveArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.arbitrary.PrimitiveWrappedArbitraryGeneratorContext;

public class FixtureMonkey {
	private static final Random seedGenerator = new Random();
	private final ArbitraryGeneratorContext generatorContext = createArbitraryGeneratorContext();

	public FixtureMonkey() {
		this(seedGenerator.nextLong());
	}

	public FixtureMonkey(long seed) {
		setUpCurrentRandom(seed);
	}

	private static ArbitraryGeneratorContext createArbitraryGeneratorContext() {
		PrimitiveWrappedArbitraryGeneratorContext primitiveWrappedArbitraryGeneratorContext =
			new PrimitiveWrappedArbitraryGeneratorContext();

		return new CompositeArbitraryGeneratorContext(
			primitiveWrappedArbitraryGeneratorContext,
			new PrimitiveArbitraryGeneratorContext(
				primitiveWrappedArbitraryGeneratorContext
			)
		);
	}

	private void setUpCurrentRandom(long seed) {
		SourceOfRandomness.create(String.valueOf(seed));
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMe(type, true);
	}

	public <T> Stream<T> giveMe(Class<T> type, boolean validOnly) {
		return this.giveMe(new ArbitraryBuilder<>(type), validOnly);
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary) {
		return this.giveMe(arbitrary, true);
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary, boolean validOnly) {
		return this.doGiveMe(arbitrary, validOnly);
	}

	public <T> Stream<T> giveMe(ArbitraryBuilder<T> builder) {
		return this.giveMe(builder, true);
	}

	public <T> Stream<T> giveMe(ArbitraryBuilder<T> builder, boolean validOnly) {
		ArbitraryGenerator<T> generator = generatorContext.get(builder.getTargetClass());

		// TODO: address NPE
		Arbitrary<T> arbitrary = generator.generate(this.generatorContext, builder);
		return this.giveMe(arbitrary, validOnly);
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type, size, true);
	}

	public <T> List<T> giveMe(Class<T> type, int size, boolean validOnly) {
		return this.giveMe(type, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> List<T> giveMe(Arbitrary<T> arbitrary, int size) {
		return this.giveMe(arbitrary, size, true);
	}

	public <T> List<T> giveMe(Arbitrary<T> arbitrary, int size, boolean validOnly) {
		return this.giveMe(arbitrary, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMeOne(type, true);
	}

	public <T> T giveMeOne(Class<T> type, boolean validOnly) {
		return this.giveMe(type, 1, validOnly).get(0);
	}

	public <T> T giveMeOne(Arbitrary<T> arbitrary) {
		return this.giveMeOne(arbitrary, true);
	}

	public <T> T giveMeOne(Arbitrary<T> arbitrary, boolean validOnly) {
		return this.giveMe(arbitrary, 1, validOnly).get(0);
	}

	private <T> Stream<T> doGiveMe(Arbitrary<T> arbitrary, boolean validOnly) {
		try {
			// TODO: filter with validator
			return arbitrary
				.generator(JqwikProperties.DEFAULT_TRIES)
				.stream(SourceOfRandomness.current())
				.map(Shrinkable::value);
		} catch (TooManyFilterMissesException ex) {
			// TODO: log error message with constraint violation messages.
			throw ex;
		}
	}
}

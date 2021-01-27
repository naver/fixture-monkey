package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.TooManyFilterMissesException;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryGenerator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.arbitrary.CompositeArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.arbitrary.PrimitiveArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.specimen.SpecimenBuilder;

public class FixtureMonkey {
	private final ArbitraryGeneratorContext generatorContext = new CompositeArbitraryGeneratorContext(
		new PrimitiveArbitraryGeneratorContext()
	);

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMe(type, true);
	}

	public <T> Stream<T> giveMe(Class<T> type, boolean validOnly) {
		return this.giveMe(new SpecimenBuilder<>(type), validOnly);
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary) {
		return this.giveMe(arbitrary, true);
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary, boolean validOnly) {
		return this.doGiveMe(arbitrary, validOnly);
	}

	public <T> Stream<T> giveMe(SpecimenBuilder<T> builder) {
		return this.giveMe(builder, true);
	}

	public <T> Stream<T> giveMe(SpecimenBuilder<T> builder, boolean validOnly) {
		ArbitraryGenerator<T> generator = generatorContext.get(builder.getSpecimenClass());
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
			return arbitrary.sampleStream();    // TODO: filter with validator
		} catch (TooManyFilterMissesException ex) {
			// TODO: log error message with constraint violation messages.
			throw ex;
		}
	}
}

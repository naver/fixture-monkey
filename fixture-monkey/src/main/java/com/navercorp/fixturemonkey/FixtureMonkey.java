package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public class FixtureMonkey {
	static {
		try {
			Field field = RandomGenerator.RandomGeneratorFacade.class.getDeclaredField("implementation");
			field.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			RandomGenerator.RandomGeneratorFacade delegate = (RandomGenerator.RandomGeneratorFacade)field.get(null);
			field.set(null, new ArbitraryRandomGeneratorFacade(delegate));
		} catch (Exception ignored) {
			// ignored
		}
	}

	private final ArbitraryOption options;
	private final ArbitraryGenerator defaultGenerator;
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final Map<Class<?>, ArbitraryGenerator> generatorMap;
	private final ArbitraryCustomizers arbitraryCustomizers;

	@SuppressWarnings("rawtypes")
	public FixtureMonkey(
		ArbitraryOption options,
		ArbitraryGenerator defaultGenerator,
		ArbitraryValidator validator,
		Map<Class<?>, ArbitraryGenerator> generatorMap,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.options = options;
		this.defaultGenerator = defaultGenerator;
		this.validator = validator;
		this.generatorMap = generatorMap;
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	public static FixtureMonkeyBuilder builder() {
		return new FixtureMonkeyBuilder();
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMeBuilder(type, options).build().sampleStream();
	}

	public <T> Stream<T> giveMe(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMeBuilder(type, options, customizer).build().sampleStream();
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type).limit(size).collect(toList());
	}

	public <T> List<T> giveMe(Class<T> type, int size, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, customizer).limit(size).collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	public <T> T giveMeOne(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, 1, customizer).get(0);
	}

	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type, options).build();
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz) {
		return this.giveMeBuilder(clazz, options);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz, ArbitraryOption options) {
		return new ArbitraryBuilder<>(clazz, options, getGenerator(clazz), validator, new ArbitraryCustomizers());
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		return new ArbitraryBuilder<>(
			value,
			new ArbitraryTraverser(options),
			getGenerator(value.getClass()),
			validator,
			this.arbitraryCustomizers
		);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryCustomizer<T> customizer
	) {
		ArbitraryCustomizers newArbitraryCustomizers =
			this.arbitraryCustomizers.mergeWith(Collections.singletonMap(clazz, customizer));
		return new ArbitraryBuilder<>(
			clazz,
			options,
			this.getGenerator(clazz, newArbitraryCustomizers),
			this.validator,
			newArbitraryCustomizers
		);
	}

	private <T> ArbitraryGenerator getGenerator(Class<T> type) {
		return this.getGenerator(type, this.arbitraryCustomizers);
	}

	private <T> ArbitraryGenerator getGenerator(Class<T> type, ArbitraryCustomizers arbitraryCustomizers) {
		ArbitraryGenerator generator = this.generatorMap.getOrDefault(type, this.defaultGenerator);
		if (generator instanceof WithFixtureCustomizer) {
			generator = ((WithFixtureCustomizer)generator).withFixtureCustomizers(arbitraryCustomizers);
		}
		return generator;
	}
}

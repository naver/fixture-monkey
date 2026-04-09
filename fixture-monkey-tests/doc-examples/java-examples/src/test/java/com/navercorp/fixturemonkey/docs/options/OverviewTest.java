package com.navercorp.fixturemonkey.docs.options;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import lombok.Value;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.arbitraries.StringArbitrary;
import org.junit.jupiter.api.Test;

class OverviewTest {

	@Value
	public static class Product {
		String productName;
		long price;
		String category;
		List<String> items;
	}

	@Test
	void testDefaultNotNullOption() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		Product product = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(product.getProductName()).isNotNull();
		then(product.getPrice()).isNotNull();
		then(product.getCategory()).isNotNull();
	}

	@Test
	void testJavaTypeArbitraryGeneratorOption() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return Arbitraries.strings().alpha().ofLength(10);
						}
					})
			)
			.build();

		// when
		String generatedString = fixtureMonkey.giveMeOne(String.class);

		// then
		then(generatedString).hasSize(10);
		then(generatedString).matches("[a-zA-Z]+");
	}

	@Test
	void testRegisterOption() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(
				Product.class,
				fm -> fm.giveMeBuilder(Product.class)
					.set("price", Arbitraries.longs().greaterOrEqual(1))
					.set("category", "Electronics")
			)
			.build();

		// when
		Product product = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(product.getPrice()).isPositive();
		then(product.getCategory()).isEqualTo("Electronics");
	}

	@Test
	void testContainerSizeOption() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.build();

		// when
		List<String> stringList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});
		Map<Integer, String> map = fixtureMonkey.giveMeOne(new TypeReference<Map<Integer, String>>() {});

		// then
		then(stringList).hasSize(3);
		then(map).hasSize(3);
	}
}

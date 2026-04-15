package com.navercorp.fixturemonkey.docs.generating;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.stream.Stream;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import lombok.Value;
import net.jqwik.api.Arbitrary;
import org.junit.jupiter.api.Test;

class FixtureMonkeyApiTest {

	@Value
	public static class Product {
		long id;
		String name;
		double price;
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void giveMeOne() {
		Product product = fixtureMonkey.giveMeOne(Product.class);

		List<String> strList = fixtureMonkey.giveMeOne(new TypeReference<List<String>>() {});

		then(product).isNotNull();
		then(strList).isNotNull();
	}

	@Test
	void giveMe() {
		Stream<Product> productStream = fixtureMonkey.giveMe(Product.class);

		Stream<List<String>> strListStream = fixtureMonkey.giveMe(new TypeReference<List<String>>() {});

		List<Product> productList = fixtureMonkey.giveMe(Product.class, 3);

		List<List<String>> strListList = fixtureMonkey.giveMe(new TypeReference<List<String>>() {}, 3);

		then(productStream).isNotNull();
		then(strListStream).isNotNull();
		then(productList).hasSize(3);
		then(strListList).hasSize(3);
	}

	@Test
	void giveMeBuilder() {
		ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

		ArbitraryBuilder<List<String>> strListBuilder = fixtureMonkey.giveMeBuilder(new TypeReference<List<String>>() {});

		then(productBuilder.sample()).isNotNull();
		then(strListBuilder.sample()).isNotNull();
	}

	@Test
	void giveMeBuilderWithInstance() {
		Product product = new Product(1L, "Book", 9.99);

		ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(product);

		then(productBuilder.sample()).isNotNull();
	}

	@Test
	void obtainInstances() {
		ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

		Product product = productBuilder.sample();
		List<Product> productList = productBuilder.sampleList(3);
		Stream<Product> productStream = productBuilder.sampleStream();

		then(product).isNotNull();
		then(productList).hasSize(3);
		then(productStream).isNotNull();
	}

	@Test
	void build() {
		ArbitraryBuilder<Product> productBuilder = fixtureMonkey.giveMeBuilder(Product.class);

		Arbitrary<Product> productArbitrary = productBuilder.build();

		then(productArbitrary).isNotNull();
	}

	@Test
	void giveMeArbitrary() {
		Arbitrary<Product> productArbitrary = fixtureMonkey.giveMeArbitrary(Product.class);

		Arbitrary<List<String>> strListArbitrary = fixtureMonkey.giveMeArbitrary(new TypeReference<List<String>>() {});

		then(productArbitrary).isNotNull();
		then(strListArbitrary).isNotNull();
	}
}

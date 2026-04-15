package com.navercorp.fixturemonkey.docs.generating;

import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import org.junit.jupiter.api.Test;

class InstantiateMethodsTest {

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.defaultNotNull(true)
		.build();

	public static class SimpleProduct {
		private final String name;
		private final int price;

		public SimpleProduct(String name, int price) {
			this.name = name;
			this.price = price;
		}

		public String getName() {
			return name;
		}

		public int getPrice() {
			return price;
		}
	}

	public static class Product {
		private final long id;
		private final String name;
		private final long price;
		private final List<String> options;

		public Product() {
			this.id = 0;
			this.name = "defaultProduct";
			this.price = 0;
			this.options = null;
		}

		public Product(String name, long price) {
			this.id = new Random().nextLong();
			this.name = name;
			this.price = price;
			this.options = Collections.emptyList();
		}

		public Product(String name, long price, List<String> options) {
			this.id = new Random().nextLong();
			this.name = name;
			this.price = price;
			this.options = options;
		}

		public static Product create(String name, long price) {
			return new Product(name, price);
		}

		public static Product createRecommended(long price) {
			return new Product("recommendedProduct", price);
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public long getPrice() {
			return price;
		}

		public List<String> getOptions() {
			return options;
		}
	}

	public static class PartiallyInitializedObject {
		private final String name;
		private int count;
		private List<String> items;

		public PartiallyInitializedObject(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public List<String> getItems() {
			return items;
		}

		public void setItems(List<String> items) {
			this.items = items;
		}
	}

	@Test
	void usingSimpleConstructor() {
		SimpleProduct product = fixtureMonkey.giveMeBuilder(SimpleProduct.class)
			.instantiate(constructor())
			.sample();

		then(product).isNotNull();
		then(product.getName()).isNotNull();
	}

	@Test
	void usingDefaultConstructor() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(constructor())
			.sample();

		then(product.getId()).isEqualTo(0);
		then(product.getName()).isEqualTo("defaultProduct");
	}

	@Test
	void selectingConstructorWithoutOptions() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				constructor()
					.parameter(String.class)
					.parameter(long.class)
			)
			.sample();

		then(product.getOptions()).isEmpty();
	}

	@Test
	void selectingConstructorWithOptions() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				constructor()
					.parameter(String.class)
					.parameter(long.class)
					.parameter(new TypeReference<List<String>>() {})
			)
			.sample();

		then(product.getOptions()).isNotNull();
	}

	@Test
	void specifyingParameterValues() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				constructor()
					.parameter(String.class, "productName")
					.parameter(long.class)
			)
			.set("productName", "specialProduct")
			.sample();

		then(product.getName()).isEqualTo("specialProduct");
	}

	@Test
	void usingFactoryMethod() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				factoryMethod("create")
			)
			.sample();

		then(product).isNotNull();
		then(product.getOptions()).isEmpty();
	}

	@Test
	void selectingSpecificFactoryMethod() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				factoryMethod("createRecommended")
					.parameter(long.class)
			)
			.sample();

		then(product.getName()).isEqualTo("recommendedProduct");
	}

	@Test
	void specifyingFactoryMethodParameterValues() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				factoryMethod("create")
					.parameter(String.class, "productName")
					.parameter(long.class, "productPrice")
			)
			.set("productName", "customProduct")
			.set("productPrice", 9900L)
			.sample();

		then(product.getName()).isEqualTo("customProduct");
		then(product.getPrice()).isEqualTo(9900L);
	}

	@Test
	void fieldBasedPropertyGeneration() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				constructor().field()
			)
			.sample();

		then(product).isNotNull();
	}

	@Test
	void javaBeanPropertyBasedGeneration() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.instantiate(
				constructor().javaBeansProperty()
			)
			.sample();

		then(product).isNotNull();
	}

	@Test
	void propertySettingAfterConstructor() {
		PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
			.instantiate(constructor().parameter(String.class).javaBeansProperty())
			.sample();

		then(obj.getName()).isNotNull();
		then(obj.getItems()).isNotNull();
	}

	@Test
	void preservingConstructorSetValues() {
		String specificName = "specificName";

		PartiallyInitializedObject obj = fixtureMonkey.giveMeBuilder(PartiallyInitializedObject.class)
			.instantiate(
				constructor()
					.parameter(String.class, "name")
			)
			.set("name", specificName)
			.sample();

		then(obj.getName()).isEqualTo(specificName);
	}
}

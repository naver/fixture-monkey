package com.navercorp.fixturemonkey.docs.generating;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;
import java.util.Arrays;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.PriorityConstructorArbitraryIntrospector;
import org.junit.jupiter.api.Test;

class IntrospectorTest {

	public static class Customer {
		private String name;
		private int age;

		public Customer() {
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}
	}

	public static class Product {
		private final String name;
		private final double price;

		@ConstructorProperties({"name", "price"})
		public Product(String name, double price) {
			this.name = name;
			this.price = price;
		}

		public String getName() {
			return name;
		}

		public double getPrice() {
			return price;
		}
	}

	public static class User {
		private final String username;
		private final String email;

		private User(Builder builder) {
			this.username = builder.username;
			this.email = builder.email;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			private String username;
			private String email;

			public Builder username(String username) {
				this.username = username;
				return this;
			}

			public Builder email(String email) {
				this.email = email;
				return this;
			}

			public User build() {
				return new User(this);
			}
		}

		public String getUsername() {
			return username;
		}

		public String getEmail() {
			return email;
		}
	}

	@Test
	void recommendedSetup() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(new FailoverIntrospector(
				Arrays.asList(
					ConstructorPropertiesArbitraryIntrospector.INSTANCE,
					BuilderArbitraryIntrospector.INSTANCE,
					FieldReflectionArbitraryIntrospector.INSTANCE,
					BeanArbitraryIntrospector.INSTANCE
				),
				false
			))
			.build();

		// when
		Customer customer = fixtureMonkey.giveMeOne(Customer.class);

		// then
		then(customer).isNotNull();
	}

	@Test
	void simplestApproach() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.build();

		// when
		Customer customer = fixtureMonkey.giveMeOne(Customer.class);

		// then
		then(customer).isNotNull();
	}

	@Test
	void testCustomer() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		Customer customer = fixtureMonkey.giveMeOne(Customer.class);

		// then
		then(customer.getName()).isNotNull();
	}

	@Test
	void testProduct() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		Product product = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(product.getName()).isNotNull();
	}

	@Test
	void testUser() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		User user = fixtureMonkey.giveMeOne(User.class);

		// then
		then(user.getUsername()).isNotNull();
		then(user.getEmail()).isNotNull();
	}

	@Test
	void beanIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.build();

		then(fixtureMonkey.giveMeOne(Customer.class)).isNotNull();
	}

	@Test
	void constructorPropertiesIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		then(fixtureMonkey.giveMeOne(Product.class)).isNotNull();
	}

	@Test
	void fieldReflectionIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.build();

		then(fixtureMonkey.giveMeOne(Customer.class)).isNotNull();
	}

	@Test
	void builderIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			.build();

		then(fixtureMonkey.giveMeOne(User.class)).isNotNull();
	}

	@Test
	void failoverIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(new FailoverIntrospector(
				Arrays.asList(
					ConstructorPropertiesArbitraryIntrospector.INSTANCE,
					BuilderArbitraryIntrospector.INSTANCE,
					FieldReflectionArbitraryIntrospector.INSTANCE,
					BeanArbitraryIntrospector.INSTANCE
				),
				false
			))
			.build();

		then(fixtureMonkey.giveMeOne(Customer.class)).isNotNull();
	}

	@Test
	void priorityConstructorIntrospector() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
			.build();

		then(fixtureMonkey.giveMeOne(Product.class)).isNotNull();
	}
}

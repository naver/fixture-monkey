package com.navercorp.fixturemonkey.docs.plugins;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import lombok.Value;
import org.junit.jupiter.api.Test;

class InterfacePluginFeaturesTest {

	public interface Animal {
		String getName();
		String sound();
	}

	@Value
	public static class Dog implements Animal {
		String name;

		@Override
		public String sound() {
			return "Woof";
		}
	}

	@Value
	public static class Cat implements Animal {
		String name;

		@Override
		public String sound() {
			return "Meow";
		}
	}

	@Test
	void interfaceImplements() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(Animal.class, Arrays.asList(Dog.class, Cat.class))
			)
			.build();

		// when
		Animal animal = fixtureMonkey.giveMeOne(Animal.class);

		// then
		then(animal).isNotNull();
		then(animal).satisfiesAnyOf(
			a -> then(a).isInstanceOf(Dog.class),
			a -> then(a).isInstanceOf(Cat.class)
		);
	}

	@Test
	void anonymousIntrospector() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(
				new InterfacePlugin()
					.useAnonymousArbitraryIntrospector(true)
			)
			.build();

		// when
		Animal animal = fixtureMonkey.giveMeOne(Animal.class);

		// then
		then(animal).isNotNull();
		then(animal.getName()).isNotNull();
	}
}

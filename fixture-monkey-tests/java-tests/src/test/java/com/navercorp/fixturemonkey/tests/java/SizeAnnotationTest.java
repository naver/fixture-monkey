package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Map;

import javax.validation.constraints.Size;

import org.junit.jupiter.api.Test;

import lombok.Value;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

class SizeAnnotationTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.plugin(new JavaxValidationPlugin())
		.build();

	@Value
	public static class SizeAnnotatedMapObject {

		String id;

		@Size(min = 1, max = 1)
		Map<String, String> values;
	}

	@Test
	void sampleSizeAnnotatedMapWithThenApply() {
		SizeAnnotatedMapObject actual = SUT.giveMeBuilder(SizeAnnotatedMapObject.class)
			.set("id", "base")
			.thenApply((obj, builder) -> builder.set("id", obj.getId() + "-applied"))
			.sample();

		then(actual.getId()).isEqualTo("base-applied");
		then(actual.getValues()).hasSize(1);
	}
}

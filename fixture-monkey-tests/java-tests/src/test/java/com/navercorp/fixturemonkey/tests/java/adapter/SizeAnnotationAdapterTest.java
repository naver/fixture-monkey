package com.navercorp.fixturemonkey.tests.java.adapter;

import java.util.Map;

import javax.validation.constraints.Size;

import org.junit.jupiter.api.Test;

import lombok.Value;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

class SizeAnnotationAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.plugin(new JavaxValidationPlugin())
		.plugin(new JavaNodeTreeAdapterPlugin().tracer(AdapterTracer.console()))
		.build();

	@Value
	public static class SizeAnnotatedMapObject {

		String id;

		@Size(min = 1, max = 1)
		Map<String, String> values;
	}

	@Test
	void sampleSizeAnnotatedMapWithThenApply() {
		try {
			SizeAnnotatedMapObject actual = SUT.giveMeBuilder(SizeAnnotatedMapObject.class)
				.set("id", "base")
				.thenApply((obj, builder) -> builder.set("id", obj.getId() + "-applied"))
				.sample();

			System.out.println("SUCCESS: " + actual);
		} catch (Exception e) {
			System.out.println("FAILED: " + e.getMessage());
			if (e.getCause() != null) {
				System.out.println("CAUSE: " + e.getCause().getMessage());
				if (e.getCause().getCause() != null) {
					System.out.println("ROOT CAUSE: " + e.getCause().getCause().getMessage());
				}
			}
			throw e;
		}
	}
}

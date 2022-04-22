package com.navercorp.fixturemonkey.api.matcher;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class AssignableMatcherTest {
	@Test
	void match() {
		// given
		Matcher sut = new AssignableTypeMatcher(String.class);

		String propertyName = "str";
		TypeReference<TypeMatcherSpec> typeReference = new TypeReference<TypeMatcherSpec>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		boolean actual = sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Test
	void matchInherited() {
		// given
		Matcher sut = new AssignableTypeMatcher(TypeMatcherSpec.class);

		String propertyName = "inherited";
		TypeReference<TypeMatcherSpec> typeReference = new TypeReference<TypeMatcherSpec>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null)
		);

		// when
		boolean actual = sut.match(context.getProperty());

		then(actual).isTrue();
	}
}

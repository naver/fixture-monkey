package com.navercorp.fixturemonkey.api.introspector;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.BDDAssertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;


import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;

public class BuilderArbitraryIntrospectorTest {
	private final ArbitraryIntrospector arbitraryIntrospector = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new EnumIntrospector(),
			new BooleanIntrospector(),
			new UuidIntrospector(),
			new JavaArbitraryIntrospector(),
			new JavaTimeArbitraryIntrospector()
		)
	);

	@Test
	void introspect() {
		// given
		BuilderArbitraryIntrospector sut = BuilderArbitraryIntrospector.INSTANCE;

		TypeReference<BuilderSample> typeReference = new TypeReference<BuilderSample>() {
		};
		RootProperty rootProperty = new RootProperty(typeReference.getAnnotatedType());

		GenerateOptions generateOptions = GenerateOptions.DEFAULT_GENERATE_OPTIONS;
		ArbitraryPropertyGenerator rootGenerator = generateOptions.getArbitraryPropertyGenerator(rootProperty);
		ArbitraryProperty rootArbitraryProperty = rootGenerator.generate(
			new ArbitraryPropertyGeneratorContext(
				rootProperty,
				null,
				null,
				null,
				generateOptions
			)
		);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> generateOptions.getArbitraryPropertyGenerator(it)
				.generate(
					new ArbitraryPropertyGeneratorContext(
						it,
						null,
						rootArbitraryProperty,
						null,
						generateOptions
					)
				)
			)
			.collect(toList());

		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			rootArbitraryProperty,
			childrenProperties,
			null,
			(ctx, prop) -> this.arbitraryIntrospector.introspect(
				new ArbitraryGeneratorContext(
					prop,
					Collections.emptyList(),
					ctx,
					(ctx2, prop2) -> null,
					Collections.emptyList()
				)
			).getValue(),
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		BuilderSample sample = (BuilderSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	 static class BuilderSample {
		private String name;
		private int value;

		BuilderSample(String name, int value) {
			this.name = name;
			this.value = value;
		}

		 public String getName() {
			 return name;
		 }

		 public int getValue() {
			 return value;
		 }

		 public static BuilderSampleBuilder builder() {
			return new BuilderSampleBuilder();
		}

		public static class BuilderSampleBuilder {

			private String name;
			private int value;

			BuilderSampleBuilder() {
			}

			public BuilderSampleBuilder name(String name) {
				this.name = name;
				return this;
			}

			public BuilderSampleBuilder value(int value) {
				this.value = value;
				return this;
			}

			public BuilderSample build() {
				return new BuilderSample(name, value);
			}
		}
	}
}

/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.api.generator;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class DefaultArbitraryGeneratorTest {
	@Test
	void generateString() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(String.class);
	}

	@Test
	void generateInteger() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "integer";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(Integer.class);
	}

	@Test
	void generateInstant() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(Instant.class);
	}

	@Test
	void generateLocalDateTime() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "localDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(LocalDateTime.class);
	}

	@Test
	void generateBoolean() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "booleans";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(Boolean.class);
	}

	@Test
	void generateEnum() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "enums";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(Season.class);
	}

	@Test
	void generateUuid() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		String propertyName = "uuid";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
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
			(ctx, prop) -> sut.generate(ctx)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(UUID.class);
	}

	@Test
	void generateBean() {
		// given
		DefaultArbitraryGenerator sut = new DefaultArbitraryGenerator();
		TypeReference<SampleArbitraryGenerator> typeReference = new TypeReference<SampleArbitraryGenerator>() {
		};
		Property rootProperty = new RootProperty(typeReference.getAnnotatedType());
		GenerateOptions generateOptions = GenerateOptions.builder()
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					0.0d, false, false, Collections.emptySet(), Collections.emptySet()
				)
			)
			.build();

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
			(ctx, prop) -> sut.generate(
				new ArbitraryGeneratorContext(
					prop,
					Collections.emptyList(),
					ctx,
					(ctx2, prop2) -> null
				)
			)
		);

		// when
		Arbitrary<?> actual = sut.generate(context);

		// then
		Object sample = actual.sample();
		then(sample).isNotNull();
		then(sample).isExactlyInstanceOf(SampleArbitraryGenerator.class);

		SampleArbitraryGenerator result = (SampleArbitraryGenerator)sample;
		then(result.getStr()).isNotNull();
		then(result.getInteger()).isNotNull();
		then(result.getInstant()).isNotNull();
		then(result.getLocalDateTime()).isNotNull();
		then(result.getBooleans()).isNotNull();
		then(result.getEnums()).isNotNull();
		then(result.getUuid()).isNotNull();
	}

	public static class SampleArbitraryGenerator {
		private String str;
		private Integer integer;
		private Instant instant;
		private LocalDateTime localDateTime;
		private Boolean booleans;
		private Season enums;
		private UUID uuid;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public Integer getInteger() {
			return integer;
		}

		public void setInteger(Integer integer) {
			this.integer = integer;
		}

		public Instant getInstant() {
			return instant;
		}

		public void setInstant(Instant instant) {
			this.instant = instant;
		}

		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}

		public void setLocalDateTime(LocalDateTime localDateTime) {
			this.localDateTime = localDateTime;
		}

		public Boolean getBooleans() {
			return booleans;
		}

		public void setBooleans(Boolean booleans) {
			this.booleans = booleans;
		}

		public Season getEnums() {
			return enums;
		}

		public void setEnums(Season enums) {
			this.enums = enums;
		}

		public UUID getUuid() {
			return uuid;
		}

		public void setUuid(UUID uuid) {
			this.uuid = uuid;
		}
	}

	enum Season {
		SPRING,
		SUMMER,
		FALL,
		WINTER
	}
}

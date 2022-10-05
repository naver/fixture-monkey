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

package com.navercorp.fixturemonkey.api.option;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArrayContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.EntryContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapEntryElementContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SingleValueObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.StreamContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.TupleLikeElementsPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptions {
	public static final List<MatcherOperator<ObjectPropertyGenerator>> DEFAULT_OBJECT_PROPERTY_GENERATORS =
		getDefaultObjectPropertyGenerators();
	public static final List<MatcherOperator<ContainerPropertyGenerator>> DEFAULT_CONTAINER_PROPERTY_GENERATORS =
		getDefaultContainerPropertyGenerators();
	public static final List<MatcherOperator<Boolean>> DEFAULT_UNIQUE_PROPERTIES = getDefaultUniqueProperties();
	public static final ObjectPropertyGenerator DEFAULT_OBJECT_PROPERTY_GENERATOR =
		DefaultObjectPropertyGenerator.INSTANCE;
	public static final PropertyNameResolver DEFAULT_PROPERTY_NAME_RESOLVER = PropertyNameResolver.IDENTITY;
	public static final int DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE = 3;
	public static final GenerateOptions DEFAULT_GENERATE_OPTIONS = GenerateOptions.builder().build();

	private final List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators;
	private final ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final PropertyNameResolver defaultPropertyNameResolver;
	private final List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators;
	private final NullInjectGenerator defaultNullInjectGenerator;
	private final List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators;
	private final int defaultArbitraryContainerSize;
	private final ArbitraryContainerInfo defaultArbitraryContainerInfo;
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;
	private final ArbitraryGenerator defaultArbitraryGenerator;

	@SuppressWarnings("rawtypes")
	private final List<MatcherOperator<FixtureCustomizer>> arbitraryCustomizers;
	private final List<MatcherOperator<Boolean>> uniqueProperties;

	@SuppressWarnings("rawtypes")
	public GenerateOptions(
		List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators,
		ObjectPropertyGenerator defaultObjectPropertyGenerator,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		int defaultArbitraryContainerSize, ArbitraryContainerInfo defaultArbitraryContainerInfo,
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		ArbitraryGenerator defaultArbitraryGenerator,
		List<MatcherOperator<FixtureCustomizer>> arbitraryCustomizers,
		List<MatcherOperator<Boolean>> uniqueProperties
	) {
		this.objectPropertyGenerators = objectPropertyGenerators;
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		this.containerPropertyGenerators = containerPropertyGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		this.nullInjectGenerators = nullInjectGenerators;
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		this.defaultArbitraryContainerSize = defaultArbitraryContainerSize;
		this.defaultArbitraryContainerInfo = defaultArbitraryContainerInfo;
		this.arbitraryGenerators = arbitraryGenerators;
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		this.arbitraryCustomizers = arbitraryCustomizers;
		this.uniqueProperties = uniqueProperties;
	}

	public static GenerateOptionsBuilder builder() {
		return new GenerateOptionsBuilder();
	}

	public List<MatcherOperator<ObjectPropertyGenerator>> getObjectPropertyGenerators() {
		return objectPropertyGenerators;
	}

	public ObjectPropertyGenerator getObjectPropertyGenerator(Property property) {
		return this.getObjectPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultObjectPropertyGenerator());
	}

	public ObjectPropertyGenerator getDefaultObjectPropertyGenerator() {
		return defaultObjectPropertyGenerator;
	}

	public List<MatcherOperator<ContainerPropertyGenerator>> getContainerPropertyGenerators() {
		return containerPropertyGenerators;
	}

	@Nullable
	public ContainerPropertyGenerator getContainerPropertyGenerator(Property property) {
		return this.getContainerPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(null);
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers;
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.getPropertyNameResolvers().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultPropertyNameResolver());
	}

	public PropertyNameResolver getDefaultPropertyNameResolver() {
		return this.defaultPropertyNameResolver;
	}

	public List<MatcherOperator<NullInjectGenerator>> getNullInjectGenerators() {
		return this.nullInjectGenerators;
	}

	public NullInjectGenerator getNullInjectGenerator(Property property) {
		return this.getNullInjectGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultNullInjectGenerator());
	}

	public NullInjectGenerator getDefaultNullInjectGenerator() {
		return this.defaultNullInjectGenerator;
	}

	public List<MatcherOperator<ArbitraryContainerInfoGenerator>> getArbitraryContainerInfoGenerators() {
		return this.arbitraryContainerInfoGenerators;
	}

	public ArbitraryContainerInfoGenerator getArbitraryContainerInfoGenerator(Property property) {
		return this.getArbitraryContainerInfoGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(context -> this.getDefaultArbitraryContainerInfo());
	}

	public int getDefaultArbitraryContainerSize() {
		return this.defaultArbitraryContainerSize;
	}

	public ArbitraryContainerInfo getDefaultArbitraryContainerInfo() {
		return this.defaultArbitraryContainerInfo;
	}

	public List<MatcherOperator<ArbitraryGenerator>> getArbitraryGenerators() {
		return this.arbitraryGenerators;
	}

	public ArbitraryGenerator getArbitraryGenerator(Property property) {
		return this.getArbitraryGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultArbitraryGenerator());
	}

	public ArbitraryGenerator getDefaultArbitraryGenerator() {
		return this.defaultArbitraryGenerator;
	}

	@SuppressWarnings("rawtypes")
	public List<MatcherOperator<FixtureCustomizer>> getArbitraryCustomizers() {
		return arbitraryCustomizers;
	}

	public List<MatcherOperator<Boolean>> getUniqueProperties() {
		return uniqueProperties;
	}

	public GenerateOptionsBuilder toBuilder() {
		return builder()
			.arbitraryObjectPropertyGenerators(objectPropertyGenerators)
			.defaultObjectPropertyGenerator(defaultObjectPropertyGenerator)
			.arbitraryContainerPropertyGenerators(containerPropertyGenerators)
			.propertyNameResolvers(new ArrayList<>(this.propertyNameResolvers))
			.defaultPropertyNameResolver(this.defaultPropertyNameResolver)
			.nullInjectGenerators(new ArrayList<>(this.nullInjectGenerators))
			.defaultNullInjectGenerator(this.defaultNullInjectGenerator)
			.arbitraryContainerInfoGenerators(new ArrayList<>(this.arbitraryContainerInfoGenerators))
			.defaultArbitraryContainerMaxSize(this.defaultArbitraryContainerSize)
			.defaultArbitraryContainerInfo(this.defaultArbitraryContainerInfo)
			.arbitraryGenerators(new ArrayList<>(this.arbitraryGenerators))
			.defaultArbitraryGenerator(this.defaultArbitraryGenerator)
			.uniqueProperties(new ArrayList<>(this.uniqueProperties));
	}
	// TODO: equals and hashCode and toString

	private static List<MatcherOperator<ObjectPropertyGenerator>> getDefaultObjectPropertyGenerators(
	) {
		return Arrays.asList(
			MatcherOperator.exactTypeMatchOperator(String.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				Character.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(char.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Short.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(short.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Byte.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(byte.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Double.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(double.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Float.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(float.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Integer.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(int.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Long.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(long.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				BigInteger.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				BigDecimal.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				Calendar.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(Date.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Instant.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				LocalDate.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				LocalDateTime.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				LocalTime.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				ZonedDateTime.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				MonthDay.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OffsetDateTime.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OffsetTime.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(Period.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				Duration.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(Year.class, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				YearMonth.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				ZoneOffset.class,
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(Matchers.BOOLEAN_TYPE_MATCHER, SingleValueObjectPropertyGenerator.INSTANCE),
			new MatcherOperator<>(Matchers.UUID_TYPE_MATCHER, SingleValueObjectPropertyGenerator.INSTANCE),
			new MatcherOperator<>(Matchers.ENUM_TYPE_MATCHER, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				UnidentifiableType.class,
				NullObjectPropertyGenerator.INSTANCE
			)
		);
	}

	private static List<MatcherOperator<ContainerPropertyGenerator>> getDefaultContainerPropertyGenerators() {
		return Arrays.asList(
			MatcherOperator.exactTypeMatchOperator(
				Optional.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalInt.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalLong.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalDouble.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				Stream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				IntStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				LongStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				DoubleStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				Iterable.class,
				DefaultContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				Iterator.class,
				DefaultContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(Map.class, MapContainerPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Entry.class, EntryContainerPropertyGenerator.INSTANCE),
			new MatcherOperator<>(
				property -> Types.getActualType(property.getType()).isArray(),
				ArrayContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> property.getClass() == MapEntryElementProperty.class,
				MapEntryElementContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> property.getClass() == TupleLikeElementsProperty.class,
				TupleLikeElementsPropertyGenerator.INSTANCE
			)
		);
	}

	private static List<MatcherOperator<Boolean>> getDefaultUniqueProperties() {
		return Collections.singletonList(
			new MatcherOperator<>(
				property -> property.getClass() == MapKeyElementProperty.class,
				true
			)
		);
	}
}

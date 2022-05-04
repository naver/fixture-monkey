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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.EntryArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapEntryElementArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SingleValueArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.StreamArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.TupleLikeElementsArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptions {
	public static final List<MatcherOperator<ArbitraryPropertyGenerator>> DEFAULT_ARBITRARY_PROPERTY_GENERATORS =
		getDefaultArbitraryPropertyGenerators();
	public static final ArbitraryPropertyGenerator DEFAULT_ARBITRARY_PROPERTY_GENERATOR =
		ObjectArbitraryPropertyGenerator.INSTANCE;
	public static final PropertyNameResolver DEFAULT_PROPERTY_NAME_RESOLVER = PropertyNameResolver.IDENTITY;
	public static final int DEFAULT_ARBITRARY_CONTAINER_SIZE = 3;
	public static final GenerateOptions DEFAULT_GENERATE_OPTIONS = GenerateOptions.builder().build();

	private final List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators;
	private final ArbitraryPropertyGenerator defaultArbitraryPropertyGenerator;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final PropertyNameResolver defaultPropertyNameResolver;
	private final List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators;
	private final NullInjectGenerator defaultNullInjectGenerator;
	private final List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators;
	private final int defaultArbitraryContainerSize;
	private final ArbitraryContainerInfo defaultArbitraryContainerInfo;
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;
	private final ArbitraryGenerator defaultArbitraryGenerator;

	public GenerateOptions(
		List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators,
		ArbitraryPropertyGenerator defaultArbitraryPropertyGenerator,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		int defaultArbitraryContainerSize,
		ArbitraryContainerInfo defaultArbitraryContainerInfo,
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		ArbitraryGenerator defaultArbitraryGenerator
	) {
		this.arbitraryPropertyGenerators = arbitraryPropertyGenerators;
		this.defaultArbitraryPropertyGenerator = defaultArbitraryPropertyGenerator;
		this.propertyNameResolvers = propertyNameResolvers;
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		this.nullInjectGenerators = nullInjectGenerators;
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		this.defaultArbitraryContainerSize = defaultArbitraryContainerSize;
		this.defaultArbitraryContainerInfo = defaultArbitraryContainerInfo;
		this.arbitraryGenerators = arbitraryGenerators;
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
	}

	public static GenerateOptionsBuilder builder() {
		return new GenerateOptionsBuilder();
	}

	public List<MatcherOperator<ArbitraryPropertyGenerator>> getArbitraryPropertyGenerators() {
		return this.arbitraryPropertyGenerators;
	}

	public ArbitraryPropertyGenerator getArbitraryPropertyGenerator(Property property) {
		return this.getArbitraryPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultArbitraryPropertyGenerator());
	}

	public ArbitraryPropertyGenerator getDefaultArbitraryPropertyGenerator() {
		return this.defaultArbitraryPropertyGenerator;
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

	public GenerateOptionsBuilder toBuilder() {
		return builder()
			.arbitraryPropertyGenerators(new ArrayList<>(this.arbitraryPropertyGenerators))
			.defaultArbitraryPropertyGenerator(this.defaultArbitraryPropertyGenerator)
			.propertyNameResolvers(new ArrayList<>(this.propertyNameResolvers))
			.defaultPropertyNameResolver(this.defaultPropertyNameResolver)
			.nullInjectGenerators(new ArrayList<>(this.nullInjectGenerators))
			.defaultNullInjectGenerator(this.defaultNullInjectGenerator)
			.arbitraryContainerInfoGenerators(new ArrayList<>(this.arbitraryContainerInfoGenerators))
			.defaultArbitraryContainerSize(this.defaultArbitraryContainerSize)
			.defaultArbitraryContainerInfo(this.defaultArbitraryContainerInfo)
			.arbitraryGenerators(new ArrayList<>(this.arbitraryGenerators))
			.defaultArbitraryGenerator(this.defaultArbitraryGenerator);
	}

	// TODO: equals and hashCode and toString

	private static List<MatcherOperator<ArbitraryPropertyGenerator>> getDefaultArbitraryPropertyGenerators() {
		return Arrays.asList(
			MatcherOperator.exactTypeMatchOperator(String.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Character.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(char.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Short.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(short.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Byte.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(byte.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Double.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(double.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Float.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(float.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Integer.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(int.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Long.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(long.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(BigInteger.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(BigDecimal.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Calendar.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Date.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Instant.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(LocalDate.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(LocalDateTime.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(LocalTime.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(ZonedDateTime.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(MonthDay.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(OffsetDateTime.class,
				SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(OffsetTime.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Period.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Duration.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Year.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(YearMonth.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(ZoneOffset.class, SingleValueArbitraryPropertyGenerator.INSTANCE),
			new MatcherOperator<>(Matchers.BOOLEAN_TYPE_MATCHER, SingleValueArbitraryPropertyGenerator.INSTANCE),
			new MatcherOperator<>(Matchers.UUID_TYPE_MATCHER, SingleValueArbitraryPropertyGenerator.INSTANCE),
			new MatcherOperator<>(Matchers.ENUM_TYPE_MATCHER, SingleValueArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(Optional.class, OptionalArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(OptionalInt.class, OptionalArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(OptionalLong.class, OptionalArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(OptionalDouble.class, OptionalArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Stream.class, StreamArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(IntStream.class, StreamArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(LongStream.class, StreamArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(DoubleStream.class, StreamArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Iterable.class, ContainerArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Iterator.class, ContainerArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Map.class, MapArbitraryPropertyGenerator.INSTANCE),
			MatcherOperator.assignableTypeMatchOperator(Entry.class, EntryArbitraryPropertyGenerator.INSTANCE),
			new MatcherOperator<>(
				property -> property.getClass() == MapEntryElementProperty.class,
				MapEntryElementArbitraryPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> property.getClass() == TupleLikeElementsProperty.class,
				TupleLikeElementsArbitraryPropertyGenerator.INSTANCE
			)
		);
	}
}

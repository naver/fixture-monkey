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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultArbitraryNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.MapArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SingleValueArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptions {
	public static final List<MatcherOperator<ArbitraryPropertyGenerator>> DEFAULT_ARBITRARY_PROPERTY_GENERATORS =
		getDefaultArbitraryPropertyGenerators();
	public static final List<MatcherOperator<ArbitraryGenerator>> DEFAULT_ARBITRARY_GENERATOR = Collections.emptyList();
	public static final GenerateOptions DEFAULT_GENERATE_OPTIONS = new GenerateOptions(
		DEFAULT_ARBITRARY_PROPERTY_GENERATORS,
		Collections.emptyList(),
		Collections.emptyList(),
		new DefaultArbitraryNullInjectGenerator(),
		Collections.emptyList(),
		new ArbitraryContainerInfo(0, 3),
		DEFAULT_ARBITRARY_GENERATOR
	);

	private final List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final List<MatcherOperator<ArbitraryNullInjectGenerator>> arbitraryNullInjectGenerators;
	private final ArbitraryNullInjectGenerator defaultArbitraryNullInjectGenerator;
	private final List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators;
	private final ArbitraryContainerInfo defaultArbitraryPropertyContainerInfo;
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;

	public GenerateOptions(
		List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		List<MatcherOperator<ArbitraryNullInjectGenerator>> arbitraryNullInjectGenerators,
		ArbitraryNullInjectGenerator defaultArbitraryNullInjectGenerator,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		ArbitraryContainerInfo defaultArbitraryPropertyContainerInfo,
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators
	) {
		this.arbitraryPropertyGenerators = arbitraryPropertyGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.arbitraryNullInjectGenerators = arbitraryNullInjectGenerators;
		this.defaultArbitraryNullInjectGenerator = defaultArbitraryNullInjectGenerator;
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		this.defaultArbitraryPropertyContainerInfo = defaultArbitraryPropertyContainerInfo;
		this.arbitraryGenerators = arbitraryGenerators;
	}

	public List<MatcherOperator<ArbitraryPropertyGenerator>> getArbitraryPropertyGenerators() {
		return this.arbitraryPropertyGenerators;
	}

	public ArbitraryPropertyGenerator getArbitraryPropertyGenerator(Property property) {
		return this.getArbitraryPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElseGet(ObjectArbitraryPropertyGenerator::new);
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers;
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.getPropertyNameResolvers().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(PropertyNameResolver.IDENTITY);
	}

	public List<MatcherOperator<ArbitraryNullInjectGenerator>> getArbitraryNullInjectGenerators() {
		return this.arbitraryNullInjectGenerators;
	}

	public ArbitraryNullInjectGenerator getArbitraryNullInjectGenerator(Property property) {
		return this.getArbitraryNullInjectGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultArbitraryNullInjectGenerator);
	}

	public List<MatcherOperator<ArbitraryContainerInfoGenerator>> getArbitraryContainerInfoGenerators() {
		return this.arbitraryContainerInfoGenerators;
	}

	public ArbitraryContainerInfoGenerator getArbitraryContainerInfoGenerator(Property property) {
		return this.getArbitraryContainerInfoGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(context -> this.defaultArbitraryPropertyContainerInfo);
	}

	public List<MatcherOperator<ArbitraryGenerator>> getArbitraryGenerators() {
		return this.arbitraryGenerators;
	}

	public ArbitraryGenerator getArbitraryGenerator(Property property) {
		// TODO: if can not find default ArbitraryGenerator
		return this.getArbitraryGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Can not find ArbitraryGenerator."));
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
			new MatcherOperator<>(
				property -> Iterable.class.isAssignableFrom(Types.getActualType(property.getType())),
				ContainerArbitraryPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> Iterator.class.isAssignableFrom(Types.getActualType(property.getType())),
				ContainerArbitraryPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> Map.class.isAssignableFrom(Types.getActualType(property.getType())),
				MapArbitraryPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> Entry.class.isAssignableFrom(Types.getActualType(property.getType())),
				MapArbitraryPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(property -> true, ObjectArbitraryPropertyGenerator.INSTANCE)
		);
	}
}

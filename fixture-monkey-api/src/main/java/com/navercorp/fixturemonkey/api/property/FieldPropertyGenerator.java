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

package com.navercorp.fixturemonkey.api.property;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * Generates field properties including not only declared fields but also super class fields and interface fields.
 */
@API(since = "0.5.3", status = Status.MAINTAINED)
public final class FieldPropertyGenerator implements PropertyGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(FieldPropertyGenerator.class);
	private static final Predicate<Field> CONSTANT_FIELD_PREDICATE =
		f -> Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers());

	private final Predicate<Field> fieldPredicate;
	private final Matcher matcher;

	public FieldPropertyGenerator(Predicate<Field> fieldPredicate, Matcher matcher) {
		this.fieldPredicate = fieldPredicate;
		this.matcher = matcher;
	}

	@Override
	@SuppressWarnings("argument")
	public List<Property> generateChildProperties(Property property) {
		Stream<FieldProperty> arbitraryfieldStream = TypeCache.getFieldsByName(Types.getActualType(property.getType()))
			.values()
			.stream()
			.filter(fieldPredicate.and(CONSTANT_FIELD_PREDICATE.negate()))
			.map(field -> new FieldProperty(
				Types.resolveWithTypeReferenceGenerics(property.getAnnotatedType(), field.getAnnotatedType()),
				field
			))
			.filter(matcher::match);

		Stream<Property> constantPropertyStream = TypeCache.getFieldsByName(
				Types.getActualType(property.getType())).values().stream()
			.filter(CONSTANT_FIELD_PREDICATE)
			.map(field -> {
				Object constantValue = null;
				try {
					constantValue = field.get(null); // the underlying field is a static field, the argument is ignored.
				} catch (IllegalAccessException ex) {
					LOGGER.warn("Field {} is inaccessible.", field.getName(), ex);
				}

				return new ConstantProperty(
					Types.resolveWithTypeReferenceGenerics(property.getAnnotatedType(), field.getAnnotatedType()),
					field.getName(),
					constantValue,
					Arrays.asList(field.getAnnotations())
				);
			});

		return Stream.concat(arbitraryfieldStream, constantPropertyStream)
			.collect(Collectors.toList());
	}
}

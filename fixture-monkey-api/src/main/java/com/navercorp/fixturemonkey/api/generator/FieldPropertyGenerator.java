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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

@API(since = "0.5.3", status = Status.EXPERIMENTAL)
public final class FieldPropertyGenerator implements PropertyGenerator {
	private final Predicate<Field> fieldPredicate;
	private final Matcher matcher;

	public FieldPropertyGenerator(Predicate<Field> fieldPredicate, Matcher matcher) {
		this.fieldPredicate = fieldPredicate;
		this.matcher = matcher;
	}

	@Override
	public List<Property> generateProperties(AnnotatedType annotatedType) {
		return PropertyCache.getFieldsByName(annotatedType).values().stream()
			.filter(fieldPredicate)
			.map(FieldProperty::new)
			.filter(matcher::match)
			.collect(Collectors.toList());
	}
}

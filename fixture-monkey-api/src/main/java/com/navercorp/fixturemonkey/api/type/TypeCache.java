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

package com.navercorp.fixturemonkey.api.type;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class TypeCache {
	private static final Map<Field, AnnotatedType> FIELD_ANNOTATED_TYPE_MAP = new ConcurrentHashMap<>();
	private static final Map<PropertyDescriptor, AnnotatedType> PROPERTY_DESCRIPTOR_ANNOTATED_TYPE_MAP =
		new ConcurrentHashMap<>();

	public static AnnotatedType getAnnotatedType(Field field) {
		return FIELD_ANNOTATED_TYPE_MAP.computeIfAbsent(field, Field::getAnnotatedType);
	}

	public static AnnotatedType getAnnotatedType(PropertyDescriptor propertyDescriptor) {
		return PROPERTY_DESCRIPTOR_ANNOTATED_TYPE_MAP.computeIfAbsent(
			propertyDescriptor,
			it -> it.getReadMethod().getAnnotatedReturnType()
		);
	}
}

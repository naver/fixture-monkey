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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapValueElementProperty implements Property {
	private final Property mapProperty;

	private final Property valueProperty;

	private final int sequence;

	/**
	 * It is deprecated;
	 * Use {@link #MapValueElementProperty(Property, Property, int)} instead.
	 */
	@Deprecated
	public MapValueElementProperty(
		Property mapProperty,
		AnnotatedType valueType,
		int sequence
	) {
		this(mapProperty, new TypeParameterProperty(valueType), sequence);
	}

	public MapValueElementProperty(Property mapProperty, Property valueProperty, int sequence) {
		this.mapProperty = mapProperty;
		this.valueProperty = valueProperty;
		this.sequence = sequence;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.valueProperty.getAnnotatedType();
	}

	public Property getMapProperty() {
		return mapProperty;
	}

	public AnnotatedType getValueType() {
		return valueProperty.getAnnotatedType();
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	@Nullable
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.valueProperty.getAnnotations();
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		Class<?> actualType = Types.getActualType(instance.getClass());

		if (Map.class.isAssignableFrom(actualType)) {
			Map<?, ?> map = (Map<?, ?>)instance;
			Iterator<? extends Entry<?, ?>> iterator = map.entrySet().iterator();
			int iteratorSequence = 0;
			while (iterator.hasNext()) {
				Entry<?, ?> value = iterator.next();
				if (iteratorSequence == getSequence()) {
					return value.getValue();
				}
				iteratorSequence++;
			}
		}

		if (Map.Entry.class.isAssignableFrom(actualType)) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>)instance;
			return entry.getValue();
		}

		throw new IllegalArgumentException("given value is not Map Entry. " + instance.getClass());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MapValueElementProperty that = (MapValueElementProperty)obj;
		return mapProperty.equals(that.mapProperty)
			&& valueProperty.equals(that.valueProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mapProperty, valueProperty);
	}
}

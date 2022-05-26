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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapKeyElementProperty implements Property {
	private final Property mapProperty;

	private final AnnotatedType keyType;

	private final int sequence;

	private final List<Annotation> annotations;

	public MapKeyElementProperty(
		Property mapProperty,
		AnnotatedType keyType,
		int sequence
	) {
		this.mapProperty = mapProperty;
		this.keyType = keyType;
		this.sequence = sequence;
		this.annotations = Arrays.asList(this.keyType.getAnnotations());
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.keyType;
	}

	public Property getMapProperty() {
		return mapProperty;
	}

	public AnnotatedType getKeyType() {
		return keyType;
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
		return this.annotations;
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		Class<?> actualType = Types.getActualType(obj.getClass());

		if (Map.class.isAssignableFrom(actualType)) {
			Map<?, ?> map = (Map<?, ?>)obj;
			Iterator<? extends Entry<?, ?>> iterator = map.entrySet().iterator();
			int iteratorSequence = 0;
			while (iterator.hasNext()) {
				Entry<?, ?> value = iterator.next();
				if (iteratorSequence == getSequence()) {
					return value.getKey();
				}
				iteratorSequence++;
			}
		}

		if (Map.Entry.class.isAssignableFrom(actualType)) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
			return entry.getKey();
		}

		throw new IllegalArgumentException("given value is not Map Entry. " + obj.getClass());
	}
}

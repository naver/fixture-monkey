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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapEntryElementProperty implements Property {
	private final Property mapEntryProperty;

	private final Property keyProperty;

	private final Property valueProperty;

	private final Type type = MapEntryElementType.class;

	private final AnnotatedType annotatedType = new AnnotatedType() {
		@Override
		public Type getType() {
			return type;
		}

		@Nullable
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return null;
		}

		@Override
		public Annotation[] getAnnotations() {
			return new Annotation[0];
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return new Annotation[0];
		}
	};

	public MapEntryElementProperty(
		Property mapEntryProperty,
		Property keyProperty,
		Property valueProperty
	) {
		this.mapEntryProperty = mapEntryProperty;
		this.keyProperty = keyProperty;
		this.valueProperty = valueProperty;
	}

	public Property getMapEntryProperty() {
		return this.mapEntryProperty;
	}

	public Property getKeyProperty() {
		return this.keyProperty;
	}

	public Property getValueProperty() {
		return this.valueProperty;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("elementProperty getName is not support yet.");
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		throw new UnsupportedOperationException("elementProperty getValue is not support yet.");
	}

	public static class MapEntryElementType {
	}
}

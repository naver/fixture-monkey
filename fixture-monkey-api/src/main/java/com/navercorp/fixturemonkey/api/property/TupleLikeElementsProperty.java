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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class TupleLikeElementsProperty implements Property {
	private final Property tupleLikeProperty;

	private final List<Property> elementsProperties;

	@Nullable
	private final Integer index;

	private final Type type = TupleLikeElementsType.class;

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

	public TupleLikeElementsProperty(
		Property tupleLikeProperty,
		List<Property> elementsProperties,
		@Nullable Integer index
	) {
		this.tupleLikeProperty = tupleLikeProperty;
		this.elementsProperties = elementsProperties;
		this.index = index;
	}

	public Property getTupleLikeProperty() {
		return this.tupleLikeProperty;
	}

	public List<Property> getElementsProperties() {
		return this.elementsProperties;
	}

	@Nullable
	public Integer getIndex() {
		return this.index;
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
	@Nullable
	public String getName() {
		return null;
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

	// This class only for type marking
	public static final class TupleLikeElementsType {
		private final List<Object> list = new ArrayList<>();

		public void add(Object value) {
			this.list.add(value);
		}

		public List<Object> getList() {
			return this.list;
		}
	}
}

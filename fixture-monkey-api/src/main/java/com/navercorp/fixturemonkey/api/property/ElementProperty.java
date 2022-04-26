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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ElementProperty implements Property {
	private final Property containerProperty;

	private final AnnotatedType elementType;

	@Nullable
	private final Integer index;

	@Nullable
	private final Double nullInject;

	private final List<Annotation> annotations;

	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public ElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		@Nullable Double nullInject
	) {
		this.containerProperty = containerProperty;
		this.elementType = elementType;
		this.index = index;
		this.nullInject = nullInject;
		this.annotations = Arrays.asList(this.elementType.getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.elementType;
	}

	public Property getContainerProperty() {
		return this.containerProperty;
	}

	public AnnotatedType getElementType() {
		return this.elementType;
	}

	@Nullable
	public Integer getIndex() {
		return this.index;
	}

	@Nullable
	public Double getNullInject() {
		return this.nullInject;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("elementProperty getName is not support yet.");
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.annotations;
	}

	@Override
	public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return Optional.ofNullable(this.annotationsMap.get(annotationClass))
			.map(annotationClass::cast);
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		throw new UnsupportedOperationException("elementProperty getValue is not support yet.");
	}
}

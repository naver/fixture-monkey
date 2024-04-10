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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(since = "1.0.16", status = API.Status.EXPERIMENTAL)
public final class SingleElementProperty implements Property {

	private final Property property;

	private final AnnotatedType elementType;

	private final List<Annotation> annotations;

	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public SingleElementProperty(Property property, AnnotatedType elementType) {
		this.property = property;
		this.elementType = elementType;
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

	public Property getProperty() {
		return this.property;
	}

	public AnnotatedType getElementType() {
		return this.elementType;
	}

	@Nullable
	@Override
	public String getName() {
		return null;
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
	public Object getValue(Object instance) {
		return property.getValue(instance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SingleElementProperty that = (SingleElementProperty)obj;
		return property.equals(that.property)
			&& elementType.equals(that.elementType)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, elementType, annotations);
	}
}

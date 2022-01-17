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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class CompositeProperty implements Property {
	private final Property primaryProperty;
	private final Property secondaryProperty;

	public CompositeProperty(Property primaryProperty, Property secondaryProperty) {
		this.primaryProperty = primaryProperty;
		this.secondaryProperty = secondaryProperty;
	}

	public Property getPrimaryProperty() {
		return this.primaryProperty;
	}

	public Property getSecondaryProperty() {
		return this.secondaryProperty;
	}

	@Override
	public Class<?> getType() {
		return this.primaryProperty.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.primaryProperty.getAnnotatedType();
	}

	@Override
	public String getName() {
		return this.primaryProperty.getName();
	}

	@Override
	public List<Annotation> getAnnotations() {
		List<Annotation> annotations = new ArrayList<>();
		annotations.addAll(this.primaryProperty.getAnnotations());
		annotations.addAll(this.secondaryProperty.getAnnotations());
		return annotations;
	}

	@Override
	public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		Optional<T> annotation = this.primaryProperty.getAnnotation(annotationClass);
		if (!annotation.isPresent()) {
			annotation = this.secondaryProperty.getAnnotation(annotationClass);
		}

		return annotation;
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		Object result = this.primaryProperty.getValue(obj);
		if (result == null) {
			result = this.secondaryProperty.getValue(obj);
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CompositeProperty that = (CompositeProperty)o;
		return Objects.equals(primaryProperty, that.primaryProperty) && Objects.equals(
			secondaryProperty, that.secondaryProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(primaryProperty, secondaryProperty);
	}

	@Override
	public String toString() {
		return "CompositeProperty{" +
			"primaryProperty=" + primaryProperty +
			", secondaryProperty=" + secondaryProperty +
			'}';
	}
}

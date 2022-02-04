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

package com.navercorp.fixturemonkey.api.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryIntrospectorContext {
	private final Property property;
	private final ArbitraryTypeIntrospector introspector;

	public ArbitraryIntrospectorContext(
		Property property,
		ArbitraryTypeIntrospector introspector
	) {
		this.property = property;
		this.introspector = introspector;
	}

	public Property getProperty() {
		return this.property;
	}

	public Class<?> getType() {
		return this.property.getType();
	}

	public String getPropertyName() {
		return this.property.getName();
	}

	public AnnotatedType getAnnotatedType() {
		return this.property.getAnnotatedType();
	}

	public List<Annotation> getAnnotations() {
		return this.property.getAnnotations();
	}

	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		return this.property.getAnnotation(annotationClass);
	}

	public ArbitraryTypeIntrospector getIntrospector() {
		return this.introspector;
	}

	// TODO: introspect with introspector

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryIntrospectorContext that = (ArbitraryIntrospectorContext)obj;
		return property.equals(that.property) && introspector.equals(that.introspector);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, introspector);
	}

	@Override
	public String toString() {
		return "ArbitraryIntrospectorContext{"
			+ "property=" + property
			+ ", introspector=" + introspector + '}';
	}
}

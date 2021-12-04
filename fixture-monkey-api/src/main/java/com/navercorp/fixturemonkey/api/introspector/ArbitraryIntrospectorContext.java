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
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryIntrospectorContext {
	private final Class<?> type;
	private final String propertyName;
	private final AnnotatedType annotatedType;
	private final List<Annotation> annotations;
	private double nullInject;

	public ArbitraryIntrospectorContext(
		Class<?> type,
		String propertyName,
		AnnotatedType annotatedType,
		List<Annotation> annotations,
		double nullInject
	) {
		this.type = type;
		this.propertyName = propertyName;
		this.annotatedType = annotatedType;
		this.annotations = annotations;
		this.nullInject = nullInject;
	}

	public Class<?> getType() {
		return this.type;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	public List<Annotation> getAnnotations() {
		return this.annotations;
	}

	public double getNullInject() {
		return this.nullInject;
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		return (Optional<T>)annotations.stream()
			.filter(it -> it.annotationType() == annotationClass)
			.findAny();
	}
}

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
import java.lang.reflect.Method;
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

@API(since = "0.5.3", status = API.Status.EXPERIMENTAL)
public final class InterfaceJavaMethodProperty implements Property {
	private final AnnotatedType annotatedType;
	private final String name;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public InterfaceJavaMethodProperty(Method method) {
		this.annotatedType = method.getAnnotatedReturnType();
		this.name = method.getName();
		this.annotations = Arrays.asList(method.getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	@Override
	public String getName() {
		return this.name;
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
		throw new UnsupportedOperationException("Interface method should not be called.");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		InterfaceJavaMethodProperty that = (InterfaceJavaMethodProperty)obj;
		return Objects.equals(annotatedType, that.annotatedType)
			&& Objects.equals(annotations, that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotatedType, annotations);
	}
}

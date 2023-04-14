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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * An interface method property for Java.
 */
@API(since = "0.5.5", status = API.Status.EXPERIMENTAL)
public final class InterfaceJavaMethodProperty implements MethodProperty {
	private final AnnotatedType returnAnnotatedType;
	private final String name;
	private final String methodName;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public InterfaceJavaMethodProperty(
		AnnotatedType returnAnnotatedType,
		String name,
		String methodName,
		List<Annotation> annotations,
		Map<Class<? extends Annotation>, Annotation> annotationsMap
	) {
		this.returnAnnotatedType = returnAnnotatedType;
		this.name = name;
		this.methodName = methodName;
		this.annotations = annotations;
		this.annotationsMap = annotationsMap;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.returnAnnotatedType;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getMethodName() {
		return methodName;
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
		return returnAnnotatedType.getType().equals(that.returnAnnotatedType.getType())
			&& Objects.equals(annotations, that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(returnAnnotatedType.getType(), annotations);
	}
}

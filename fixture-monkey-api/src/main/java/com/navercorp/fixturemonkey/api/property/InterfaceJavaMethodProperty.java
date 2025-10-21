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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * An interface method property for Java.
 */
@API(since = "0.5.5", status = Status.MAINTAINED)
public final class InterfaceJavaMethodProperty implements MethodProperty {
	private final JvmType returnJvmType;
	private final String name;
	private final String methodName;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public InterfaceJavaMethodProperty(
		AnnotatedType returnAnnotatedType,
		String name,
		String methodName,
		List<Annotation> annotations
	) {
		this.returnJvmType = Types.toJvmType(returnAnnotatedType, annotations);
		this.name = name;
		this.methodName = methodName;
		this.annotationsMap = annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.returnJvmType.getRawType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.returnJvmType.getAnnotatedType();
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
		return this.returnJvmType.getAnnotations();
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
		return returnJvmType.equals(that.returnJvmType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(returnJvmType);
	}
}

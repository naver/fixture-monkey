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
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A {@link Property} implementation representing a concrete type,
 * where the concrete type may be different from the {@code abstractTypeProperty}.
 * <p>
 * The {@code abstractTypeProperty} is a property of the abstract type that actually resides in the metadata of a class.
 * The {@code concreteAnnotatedType} is the annotated type of the concrete type that is resolved as the actual type.
 */
@API(since = "1.1.7", status = Status.EXPERIMENTAL)
public final class ConcreteTypeProperty implements Property {
	private static final Logger log = LoggerFactory.getLogger(ConcreteTypeProperty.class);

	private final JvmType jvmType;
	private final Property abstractTypeProperty;

	public ConcreteTypeProperty(AnnotatedType concreteAnnotatedType, Property abstractTypeProperty) {
		this.jvmType = Types.toJvmType(concreteAnnotatedType, Collections.emptyList());
		this.abstractTypeProperty = abstractTypeProperty;
	}

	public ConcreteTypeProperty(
		AnnotatedType concreteAnnotatedType,
		Property abstractTypeProperty,
		List<Annotation> annotations
	) {
		this.jvmType = Types.toJvmType(concreteAnnotatedType, annotations);
		this.abstractTypeProperty = abstractTypeProperty;
	}

	@Override
	public Type getType() {
		return jvmType.getRawType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return jvmType.getAnnotatedType();
	}

	@Nullable
	@Override
	public String getName() {
		return abstractTypeProperty.getName();
	}

	@Override
	public List<Annotation> getAnnotations() {
		List<Annotation> concatAnnotations = new ArrayList<>(this.jvmType.getAnnotations());
		concatAnnotations.addAll(abstractTypeProperty.getAnnotations());
		return Collections.unmodifiableList(concatAnnotations);
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		return abstractTypeProperty.getValue(instance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ConcreteTypeProperty that = (ConcreteTypeProperty)obj;
		return Objects.equals(jvmType, that.jvmType)
			&& Objects.equals(abstractTypeProperty, that.abstractTypeProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jvmType, abstractTypeProperty);
	}
}

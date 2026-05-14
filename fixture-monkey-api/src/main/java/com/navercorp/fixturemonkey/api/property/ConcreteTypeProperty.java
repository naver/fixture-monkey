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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A {@link Property} implementation representing a concrete type,
 * where the concrete type may be different from the {@code abstractTypeProperty}.
 * <p>
 * The {@code abstractTypeProperty} is a property of the abstract type that actually resides in the metadata of a class.
 */
@API(since = "1.1.7", status = Status.EXPERIMENTAL)
public final class ConcreteTypeProperty implements Property {
	private static final Logger log = LoggerFactory.getLogger(ConcreteTypeProperty.class);

	private final JvmType jvmType;
	private final Property abstractTypeProperty;

	public ConcreteTypeProperty(JvmType jvmType, Property abstractTypeProperty) {
		this.jvmType = jvmType;
		this.abstractTypeProperty = abstractTypeProperty;
	}

	@Override
	public JvmType getJvmType() {
		List<Annotation> combinedAnnotations = getAnnotations();
		if (jvmType.getAnnotations().equals(combinedAnnotations)) {
			return jvmType;
		}
		return new JavaType(
			jvmType.getRawType(),
			jvmType.getTypeVariables(),
			combinedAnnotations,
			jvmType.getComponentType(),
			jvmType.getNullable()
		);
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

	@Override
	public boolean equals(@Nullable Object obj) {
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

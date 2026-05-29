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
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

/**
 * It is a property for a fixed single element of a container. ex, Optional, Function, Supplier
 * It can be nested. For example, {@code Optional<Optional<String>>}.
 * <p>
 * The main differences between {@link SingleElementProperty} and {@link DefaultContainerElementProperty} are:
 * - {@link SingleElementProperty} is used for a fixed single element of a container.
 * It has no explict sequence and index. For example, {@code Optional<String> optional},
 * it can be referenced by {@code optional}.
 * - {@link DefaultContainerElementProperty} is used for an element of a container that can have multiple elements.
 * It has an explicit sequence and index. For example, {@code List<String> list},
 * it can be referenced by {@code list[0]}, {@code list[1]}.
 */
@API(since = "1.0.17", status = API.Status.EXPERIMENTAL)
public final class SingleElementProperty implements ContainerElementProperty {
	private final Property containerProperty;

	private final Property elementProperty;

	/**
	 * It is deprecated.
	 * Use {@link #SingleElementProperty(Property, Property)} instead.
	 */
	@Deprecated
	public SingleElementProperty(Property containerProperty) {
		this.containerProperty = containerProperty;
		this.elementProperty = new TypeParameterProperty(containerProperty.getJvmType());
	}

	public SingleElementProperty(Property containerProperty, Property elementProperty) {
		this.containerProperty = containerProperty;
		this.elementProperty = elementProperty;
	}

	@Override
	public JvmType getJvmType() {
		JvmType base = this.elementProperty.getJvmType();
		List<Annotation> annotations = getAnnotations();
		if (base.getAnnotations().equals(annotations)) {
			return base;
		}
		return new ReflectiveJvmType(
			base.getRawType(),
			base.getTypeVariables(),
			annotations,
			base.getComponentType(),
			base.getNullable()
		);
	}

	@Nullable
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.containerProperty.getAnnotations();
	}

	@Override
	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this;
	}

	@Override
	public int getSequence() {
		return 0;
	}

	@Nullable
	@Override
	public Integer getIndex() {
		return null;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SingleElementProperty that = (SingleElementProperty)obj;
		return Objects.equals(containerProperty, that.containerProperty)
			&& Objects.equals(elementProperty, that.elementProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementProperty);
	}
}

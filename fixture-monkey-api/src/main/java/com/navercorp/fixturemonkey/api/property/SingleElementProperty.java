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
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(since = "1.0.17", status = API.Status.EXPERIMENTAL)
public class SingleElementProperty implements Property, ContainerElementProperty {
	private final Property containerProperty;

	private final Property elementProperty;

	/**
	 * It is deprecated.
	 * Use {@link #SingleElementProperty(Property, Property)} instead.
	 */
	@Deprecated
	public SingleElementProperty(Property containerProperty) {
		this.containerProperty = containerProperty;
		this.elementProperty = new TypeParameterProperty(containerProperty.getAnnotatedType());
	}

	public SingleElementProperty(Property containerProperty, Property elementProperty) {
		this.containerProperty = containerProperty;
		this.elementProperty = elementProperty;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.elementProperty.getAnnotatedType();
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

	@Nullable
	@Override
	public Object getValue(Object instance) {
		return containerProperty.getValue(instance);
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
	public boolean equals(Object obj) {
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

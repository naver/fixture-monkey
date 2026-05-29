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
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class DefaultContainerElementProperty implements ContainerElementProperty {
	private final Property containerProperty;
	private final Property elementProperty;
	private final int sequence;
	@Nullable
	private final Integer index;

	public DefaultContainerElementProperty(
		Property containerProperty,
		Property elementProperty,
		@Nullable Integer index,
		int sequence
	) {
		this.containerProperty = containerProperty;
		this.elementProperty = elementProperty;
		this.index = index;
		this.sequence = sequence;
	}

	@Override
	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this.elementProperty;
	}

	@Override
	public int getSequence() {
		return this.sequence;
	}

	@Nullable
	@Override
	public Integer getIndex() {
		return this.index;
	}

	@Override
	public JvmType getJvmType() {
		return this.elementProperty.getJvmType();
	}

	@Nullable
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.elementProperty.getAnnotations();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DefaultContainerElementProperty that = (DefaultContainerElementProperty)obj;
		return sequence == that.sequence
			&& Objects.equals(containerProperty, that.containerProperty)
			&& Objects.equals(elementProperty, that.elementProperty)
			&& Objects.equals(index, that.index);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementProperty, sequence, index);
	}
}

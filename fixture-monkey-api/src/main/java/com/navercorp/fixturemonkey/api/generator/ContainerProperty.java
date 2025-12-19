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
package com.navercorp.fixturemonkey.api.generator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ContainerProperty {
	private final List<Property> elementProperties;

	private final ArbitraryContainerInfo containerInfo;

	public ContainerProperty(
		List<Property> elementProperties,
		ArbitraryContainerInfo containerInfo
	) {
		this.elementProperties = elementProperties;
		this.containerInfo = containerInfo;
	}

	public List<Property> getElementProperties() {
		return Collections.unmodifiableList(elementProperties);
	}

	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerProperty that = (ContainerProperty)obj;
		return elementProperties.equals(that.elementProperties) && containerInfo.equals(that.containerInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementProperties, containerInfo);
	}
}

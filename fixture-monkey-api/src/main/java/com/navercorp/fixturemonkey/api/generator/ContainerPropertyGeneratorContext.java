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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ContainerPropertyGeneratorContext {
	private final Property property;
	@Nullable
	private final ArbitraryContainerInfo containerInfo;
	@Nullable
	private final ArbitraryContainerInfoGenerator containerInfoGenerator;

	public ContainerPropertyGeneratorContext(
		Property property,
		@Nullable ArbitraryContainerInfo containerInfo,
		@Nullable ArbitraryContainerInfoGenerator containerInfoGenerator
	) {
		this.property = property;
		this.containerInfo = containerInfo;
		this.containerInfoGenerator = containerInfoGenerator;
	}

	public Property getProperty() {
		return property;
	}

	@Nullable
	@Deprecated
	public Integer getElementIndex() {
		return null;
	}

	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo != null ? containerInfo : containerInfoGenerator.generate(this);
	}

	public boolean isRootContext() {
		return this.property instanceof TreeRootProperty;
	}
}

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

import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryPropertyGeneratorContext {
	private final Property property;

	@Nullable
	private final Integer elementIndex;

	@Nullable
	private final ArbitraryProperty ownerProperty;

	@Nullable
	private final ArbitraryContainerInfo containerInfo;

	private final GenerateOptions generateOptions;

	public ArbitraryPropertyGeneratorContext(
		Property property,
		@Nullable Integer elementIndex,
		@Nullable ArbitraryProperty ownerProperty,
		@Nullable ArbitraryContainerInfo containerInfo,
		GenerateOptions generateOptions
	) {
		this.property = property;
		this.elementIndex = elementIndex;
		this.ownerProperty = ownerProperty;
		this.containerInfo = containerInfo;
		this.generateOptions = generateOptions;
	}

	public Property getProperty() {
		return this.property;
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	@Nullable
	public ArbitraryProperty getOwnerProperty() {
		return this.ownerProperty;
	}

	@Nullable
	public ArbitraryContainerInfo getContainerInfo() {
		return containerInfo;
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.generateOptions.getPropertyNameResolvers();
	}

	public GenerateOptions getGenerateOptions() {
		return this.generateOptions;
	}

	public PropertyNameResolver getPropertyNameResolver() {
		return generateOptions.getPropertyNameResolver(property);
	}

	public boolean isRootContext() {
		return this.property instanceof RootProperty;
	}
}

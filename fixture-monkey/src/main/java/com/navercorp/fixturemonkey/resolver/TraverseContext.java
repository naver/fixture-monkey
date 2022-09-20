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

package com.navercorp.fixturemonkey.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class TraverseContext {
	private final List<ArbitraryProperty> arbitraryProperties;
	private final Map<NodeResolver, ArbitraryContainerInfo> arbitraryContainerInfosByNodeResolver;

	public TraverseContext(
		List<ArbitraryProperty> arbitraryProperties,
		Map<NodeResolver, ArbitraryContainerInfo> arbitraryContainerInfosByNodeResolver
	) {
		this.arbitraryProperties = arbitraryProperties;
		this.arbitraryContainerInfosByNodeResolver = arbitraryContainerInfosByNodeResolver;
	}

	public List<ArbitraryProperty> getArbitraryProperties() {
		return arbitraryProperties;
	}

	public Map<NodeResolver, ArbitraryContainerInfo> getArbitraryContainerInfosByNodeResolver() {
		return arbitraryContainerInfosByNodeResolver;
	}

	public TraverseContext appendArbitraryProperty(
		ArbitraryProperty arbitraryProperty
	) {
		List<ArbitraryProperty> arbitraryProperties = new ArrayList<>(this.arbitraryProperties);
		arbitraryProperties.add(arbitraryProperty);
		return new TraverseContext(arbitraryProperties, arbitraryContainerInfosByNodeResolver);
	}
}

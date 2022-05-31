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

import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryNode {
	private ArbitraryProperty arbitraryProperty;

	private List<ArbitraryNode> children;

	@Nullable
	private Arbitrary<?> arbitrary;

	ArbitraryNode(
		ArbitraryProperty arbitraryProperty,
		List<ArbitraryNode> children
	) {
		this.arbitraryProperty = arbitraryProperty;
		this.children = children;
	}

	public void setArbitraryProperty(ArbitraryProperty arbitraryProperty) {
		this.arbitraryProperty = arbitraryProperty;
	}

	public void setChildren(List<ArbitraryNode> children) {
		this.children = children;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.arbitraryProperty;
	}

	public Property getProperty() {
		return this.getArbitraryProperty().getProperty();
	}

	public List<ArbitraryNode> getChildren() {
		return this.children;
	}

	@Nullable
	public Arbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public void setArbitrary(@Nullable Arbitrary<?> arbitrary) {
		this.arbitrary = arbitrary;
	}
}

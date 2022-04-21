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

package com.navercorp.fixturemonkey.traverser;

import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class ArbitraryNode {
	private ArbitraryProperty arbitraryProperty;

	@Nullable
	private ArbitraryNode parent;

	private List<ArbitraryNode> children;

	@Nullable
	private Arbitrary<?> arbitrary;

	private boolean manipulated = false;

	private boolean active = true; // isNull

	private boolean fixed = false;

	private boolean reset = false;

	ArbitraryNode(
		ArbitraryProperty arbitraryProperty,
		List<ArbitraryNode> children,
		@Nullable Arbitrary<?> arbitrary
	) {
		this.arbitraryProperty = arbitraryProperty;
		this.children = children;
		this.arbitrary = arbitrary;
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.arbitraryProperty;
	}

	@Nullable
	public ArbitraryNode getParent() {
		return this.parent;
	}

	public void setParent(ArbitraryNode parent) {
		this.parent = parent;
	}

	public List<ArbitraryNode> getChildren() {
		return this.children;
	}

	@Nullable
	public Arbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public boolean isManipulated() {
		return this.manipulated;
	}

	public void setManipulated(boolean manipulated) {
		this.manipulated = manipulated;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isFixed() {
		return this.fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isReset() {
		return this.reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}
}

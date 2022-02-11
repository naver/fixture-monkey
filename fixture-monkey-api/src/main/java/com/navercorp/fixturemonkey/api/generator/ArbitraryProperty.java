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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryProperty {
	private final Property property;
	@Nullable
	private final Integer indexOfIterable;
	private final boolean keyOfMapStructure;
	private final double nullInject;

	public ArbitraryProperty(
		Property property,
		@Nullable Integer indexOfIterable,
		boolean keyOfMapStructure,
		double nullInject
	) {
		this.property = property;
		this.indexOfIterable = indexOfIterable;
		this.keyOfMapStructure = keyOfMapStructure;
		this.nullInject = nullInject;
	}

	public static ArbitraryProperty root(RootProperty rootProperty) {
		return new ArbitraryProperty(
			rootProperty,
			null,
			false,
			0.0D
		);
	}

	public Property getProperty() {
		return this.property;
	}

	@Nullable
	public Integer getIndexOfIterable() {
		return this.indexOfIterable;
	}

	public boolean isKeyOfMapStructure() {
		return this.keyOfMapStructure;
	}

	public double getNullInject() {
		return this.nullInject;
	}

	public boolean isRoot() {
		return this.property instanceof RootProperty;
	}

	// TODO: equals and hashCode and toString
}

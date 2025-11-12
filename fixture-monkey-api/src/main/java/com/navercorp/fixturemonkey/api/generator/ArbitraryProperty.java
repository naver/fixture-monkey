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
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.ConcreteTypeDefinition;
import com.navercorp.fixturemonkey.api.tree.TreeProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryProperty extends TreeProperty {
	private final List<ConcreteTypeDefinition> concreteTypeDefinitions;
	private final double nullInject;

	public ArbitraryProperty(
		ObjectProperty objectProperty,
		boolean container,
		double nullInject,
		List<ConcreteTypeDefinition> concreteTypeDefinitions
	) {
		super(objectProperty, container, concreteTypeDefinitions);
		this.concreteTypeDefinitions = concreteTypeDefinitions;
		this.nullInject = nullInject;
	}

	public ArbitraryProperty withNullInject(double nullInject) {
		return new ArbitraryProperty(
			this.getObjectProperty(),
			this.isContainer(),
			nullInject,
			this.getConcreteTypeDefinitions()
		);
	}

	public List<ConcreteTypeDefinition> getConcreteTypeDefinitions() {
		return concreteTypeDefinitions;
	}

	public double getNullInject() {
		return nullInject;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryProperty that = (ArbitraryProperty)obj;
		return this.getObjectProperty().equals(that.getObjectProperty()) && this.isContainer() == that.isContainer();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getObjectProperty(), this.isContainer());
	}
}

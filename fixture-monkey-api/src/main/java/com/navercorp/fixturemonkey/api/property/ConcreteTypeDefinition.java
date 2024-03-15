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

import java.util.List;

/**
 * Represents a concrete type definition with a resolved concrete property and a list of child properties.
 * Instances of this class are immutable once created.
 */
public final class ConcreteTypeDefinition {
	private final Property concreteProperty;
	private final List<Property> childPropertyLists;

	public ConcreteTypeDefinition(Property concreteProperty, List<Property> childPropertyLists) {
		this.concreteProperty = concreteProperty;
		this.childPropertyLists = childPropertyLists;
	}

	public Property getConcreteProperty() {
		return concreteProperty;
	}

	public List<Property> getChildPropertyLists() {
		return childPropertyLists;
	}
}

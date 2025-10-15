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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It represents a property that is a container element.
 * Container means that the type has one or more type parameters
 * and the type has the properties (called elements) of the type parameters inside it.
 * <p>
 * For example, {@code List<String>} is a container type because it has one type parameter {@code String}.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public interface ContainerElementProperty extends Property {
	/**
	 * It returns the property of the container.
	 *
	 * @return the property of the container
	 */
	Property getContainerProperty();

	/**
	 * It returns the property of the element.
	 *
	 * @return the property of the element
	 */
	Property getElementProperty();

	/**
	 * It returns the sequence of the element.
	 *
	 * @return the sequence of the element
	 */
	int getSequence();

	/**
	 * It returns the index of the element.
	 *
	 * @return the index of the element, may be null if the container has no ordering.
	 */
	@Nullable
	Integer getIndex();
}

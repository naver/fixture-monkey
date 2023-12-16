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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Provides access to a {@link PropertyGenerator} for a given property.
 */
@API(since = "1.0.6", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface PropertyGeneratorAccessor {
	/**
	 * Retrieves a {@link PropertyGenerator} instance for the specified property.
	 * This method can return null, indicating that there is no associated PropertyGenerator for the given property.
	 *
	 * @param property The property for which a PropertyGenerator is needed.
	 *                 Cannot be null.
	 * @return A PropertyGenerator instance suitable for the specified property,
	 * or null if no such generator is available or applicable.
	 */
	@Nullable
	PropertyGenerator getPropertyGenerator(Property property);
}

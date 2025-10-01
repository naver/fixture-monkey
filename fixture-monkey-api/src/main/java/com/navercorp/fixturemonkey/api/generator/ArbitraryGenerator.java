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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

@API(since = "0.4.0", status = Status.MAINTAINED)
@FunctionalInterface
public interface ArbitraryGenerator {
	CombinableArbitrary<?> generate(ArbitraryGeneratorContext context);

	/**
	 * Retrieves a {@link PropertyGenerator} that is required for the specified property.
	 * This default implementation returns {@code null}, indicating that no specific
	 * PropertyGenerator is required by default.
	 * Implementations can override this method to provide a non-null {@link PropertyGenerator} as needed.
	 *
	 * <p>
	 * When this method returns a non-null value, it serves as the default PropertyGenerator for the specified property.
	 * If a different PropertyGenerator is provided by an option, it should take precedence over the one
	 * returned by this method.
	 * </p>
	 *
	 * @param property The {@link Property} for which the PropertyGenerator is required.
	 *                 This parameter should not be null. Implementations can choose to
	 *                 throw an exception if null is passed.
	 * @return A {@link PropertyGenerator} that is associated with the specified property,
	 *         or {@code null} if no such generator is required or available.
	 */
	@Nullable
	default PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return null;
	}
}

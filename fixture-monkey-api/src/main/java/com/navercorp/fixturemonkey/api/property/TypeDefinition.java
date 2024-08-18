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

/**
 * It is for internal use only. It can be changed or removed at any time.
 * <p>
 * It establishes the relationship between the parent and child properties.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
public interface TypeDefinition {
	/**
	 * Retrieves the resolved property.
	 *
	 * @return the resolved property, which is actually the generated property.
	 * It may be different from the class time property.
	 */
	Property getResolvedProperty();

	/**
	 * Retrieves the property generator.
	 *
	 * @return the property generator, which generates the child properties or the element properties
	 */
	PropertyGenerator getPropertyGenerator();
}

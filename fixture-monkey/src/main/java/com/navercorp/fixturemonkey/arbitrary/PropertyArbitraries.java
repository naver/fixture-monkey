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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.Arbitrary;

public class PropertyArbitraries {
	@SuppressWarnings("rawtypes")
	private final Map<String, Arbitrary> propertyArbitraries;

	@SuppressWarnings("rawtypes")
	public PropertyArbitraries(Map<String, Arbitrary> propertyArbitraries) {
		if (propertyArbitraries == null) {
			propertyArbitraries = Collections.emptyMap();
		}
		this.propertyArbitraries = new HashMap<>(propertyArbitraries);
	}

	public Set<Map.Entry<String, Arbitrary>> getPropertyArbitraries() {
		return this.propertyArbitraries.entrySet();
	}
}

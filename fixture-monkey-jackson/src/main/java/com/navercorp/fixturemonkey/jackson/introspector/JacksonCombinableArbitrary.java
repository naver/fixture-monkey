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

package com.navercorp.fixturemonkey.jackson.introspector;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;

/**
 * It is a {@link CombinableArbitrary} for Jackson library.
 */
@API(since = "0.5.0", status = Status.EXPERIMENTAL)
public final class JacksonCombinableArbitrary implements CombinableArbitrary {
	private final CombinableArbitrary jsonValue;
	private final Function<Object, Object> deserializer;

	public JacksonCombinableArbitrary(
		CombinableArbitrary jsonValue,
		Function<Object, Object> deserializer
	) {
		this.jsonValue = jsonValue;
		this.deserializer = deserializer;
	}

	/**
	 * It would deserialize a serialized {@code jsonValue} object into an actual object.
	 * @return an actual object
	 */
	@Override
	public Object combined() {
		return deserializer.apply(jsonValue.rawValue());
	}

	/**
	 * It would generate a serialized object.
	 * @return a map representing JsonObject or JsonArray
	 */
	@Override
	public Object rawValue() {
		return jsonValue.rawValue();
	}

	@Override
	public void clear() {
		jsonValue.clear();
	}

	@Override
	public boolean fixed() {
		return false;
	}
}

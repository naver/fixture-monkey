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

package com.navercorp.fixturemonkey.jackson.property;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JacksonPropertyNameResolver implements PropertyNameResolver {
	@SuppressWarnings("methodref.return")
	@Override
	public String resolve(Property property) {
		return property.getAnnotation(JsonProperty.class)
			.map(JsonProperty::value)
			.orElseGet(property::getName);
	}
}

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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ObjectArbitraryPropertyGenerator implements ArbitraryPropertyGenerator {
	public static final ObjectArbitraryPropertyGenerator INSTANCE = new ObjectArbitraryPropertyGenerator();

	@Override
	public ArbitraryProperty property(ArbitraryPropertyGeneratorContext context) {
		Property property = context.getProperty();
		List<Property> childProperties = PropertyCache.getProperties(property.getAnnotatedType());
		return new ArbitraryProperty(
			property,
			context.getPropertyNameResolver(),
			context.getPropertyValue(),
			context.getGenerateOptions().getNullInject(),
			context.getElementIndex(),
			childProperties,
			null
		);
	}
}

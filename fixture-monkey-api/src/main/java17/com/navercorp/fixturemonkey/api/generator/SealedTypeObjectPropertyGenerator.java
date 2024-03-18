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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyUtils;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.14", status = Status.EXPERIMENTAL)
public final class SealedTypeObjectPropertyGenerator implements ObjectPropertyGenerator {
	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property sealedTypeProperty = context.getProperty();
		Class<?> actualType = Types.getActualType(sealedTypeProperty.getType());
		double nullInject = context.getNullInjectGenerator().generate(context);

		Map<Property, List<Property>> childPropertiesByProperty = new HashMap<>();
		for (Class<?> subClass : actualType.getPermittedSubclasses()) {
			Property subProperty = PropertyUtils.toProperty(subClass);

			List<Property> subPropertyChildProperties = context.getPropertyGenerator()
				.generateChildProperties(subProperty);

			childPropertiesByProperty.put(subProperty, subPropertyChildProperties);
		}

		return new ObjectProperty(
			sealedTypeProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childPropertiesByProperty
		);
	}
}

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

import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class DefaultObjectPropertyGenerator implements ObjectPropertyGenerator {
	public static final DefaultObjectPropertyGenerator INSTANCE =
		new DefaultObjectPropertyGenerator();

	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property property = context.getProperty();
		PropertyGenerator propertyGenerator = context.getFixtureMonkeyOptions().getPropertyGenerator(property);
		List<Property> childProperties =
			propertyGenerator.generateChildProperties(property.getAnnotatedType());
		double nullInject = context.getFixtureMonkeyOptions().getNullInjectGenerator(property)
			.generate(context);

		return new ObjectProperty(
			property,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			Collections.singletonMap(property, childProperties)
		);
	}
}

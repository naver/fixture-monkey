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

import java.beans.ConstructorProperties;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.5.3", status = Status.MAINTAINED)
public final class DefaultPropertyGenerator implements PropertyGenerator {
	private static final CompositePropertyGenerator COMPOSITE_PROPERTY_GENERATOR =
		new CompositePropertyGenerator(
			Arrays.asList(
				new ConstructorParameterPropertyGenerator(
					it -> it.getAnnotation(ConstructorProperties.class) != null
						|| Arrays.stream(it.getParameters()).anyMatch(Parameter::isNamePresent)
						|| it.getParameters().length == 0,
					it -> true
				),
				new FieldPropertyGenerator(it -> true, it -> true),
				new JavaBeansPropertyGenerator(
					it -> it.getReadMethod() != null && it.getWriteMethod() != null,
					it -> true
				)
			)
		);

	public List<Property> generateChildProperties(Property property) {
		return COMPOSITE_PROPERTY_GENERATOR.generateChildProperties(property);
	}
}

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

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

@API(since = "0.5.3", status = Status.MAINTAINED)
public final class DefaultPropertyGenerator implements PropertyGenerator {
	public static final PropertyGenerator CACHED_DEFAULT_FIELD_PROPERTY_GENERATOR = new LazyPropertyGenerator(
		new FieldPropertyGenerator(
			it -> true,
			it -> true
		)
	);

	public static final PropertyGenerator CACHED_DEFAULT_JAVA_BEANS_PROPERTY_GENERATOR = new LazyPropertyGenerator(
		new JavaBeansPropertyGenerator(
			it -> it.getReadMethod() != null && it.getWriteMethod() != null,
			it -> true
		)
	);

	public static final PropertyGenerator CACHED_DEFAULT_CONSTRUCTOR_PROPERTY_GENERATOR =
		new LazyPropertyGenerator(ConstructorPropertiesArbitraryIntrospector.PROPERTY_GENERATOR);

	public static final PropertyGenerator CACHED_DEFAULT_ANNOTATED_FIELD_PROPERTY_GENERATOR = new LazyPropertyGenerator(
		new CompositePropertyGenerator(
			Arrays.asList(
				CACHED_DEFAULT_FIELD_PROPERTY_GENERATOR,
				CACHED_DEFAULT_JAVA_BEANS_PROPERTY_GENERATOR
			)
		)
	);

	private static final PropertyGenerator CACHED_COMPOSITE_PROPERTY_GENERATOR =
		new LazyPropertyGenerator(
			new CompositePropertyGenerator(
				Arrays.asList(
					CACHED_DEFAULT_CONSTRUCTOR_PROPERTY_GENERATOR,
					CACHED_DEFAULT_ANNOTATED_FIELD_PROPERTY_GENERATOR
				)
			)
		);

	public List<Property> generateChildProperties(Property property) {
		return CACHED_COMPOSITE_PROPERTY_GENERATOR.generateChildProperties(property);
	}
}

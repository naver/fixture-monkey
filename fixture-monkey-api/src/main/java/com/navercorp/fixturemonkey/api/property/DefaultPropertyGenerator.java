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
	/**
	 * A cached field-based property generator that generates properties from fields.
	 *
	 * <p>This generator uses {@link LazyPropertyGenerator} with LRU caching to reduce
	 * unnecessary duplicate property generation during object tree construction.
	 * It includes all fields (no filtering) and all annotations.
	 *
	 * <p><strong>Note:</strong> The caching is based on property identity using a
	 * concurrent LRU cache with a maximum size of 32 entries. If the same property
	 * is accessed multiple times during object tree generation, the cached result
	 * will be reused instead of regenerating the child properties, which improves
	 * performance significantly especially for complex object hierarchies.
	 */
	public static final PropertyGenerator FIELD_PROPERTY_GENERATOR = new LazyPropertyGenerator(
		new FieldPropertyGenerator(
			it -> true,
			it -> true
		)
	);

	/**
	 * A cached JavaBeans method-based property generator that generates properties from getter/setter pairs.
	 *
	 * <p>This generator uses {@link LazyPropertyGenerator} with LRU caching to reduce
	 * unnecessary duplicate property generation during object tree construction.
	 * It only includes properties that have both read and write methods (getter and setter).
	 *
	 * <p><strong>Note:</strong> The caching is based on property identity using a
	 * concurrent LRU cache with a maximum size of 32 entries. If the same property
	 * is accessed multiple times during object tree generation, the cached result
	 * will be reused instead of regenerating the child properties, which improves
	 * performance significantly especially for complex object hierarchies.
	 */
	public static final PropertyGenerator METHOD_PROPERTY_GENERATOR = new LazyPropertyGenerator(
		new JavaBeansPropertyGenerator(
			it -> it.getReadMethod() != null && it.getWriteMethod() != null,
			it -> true
		)
	);

	public static final PropertyGenerator FIELD_METHOD_PROPERTY_GENERATOR = new CompositePropertyGenerator(
		Arrays.asList(
			FIELD_PROPERTY_GENERATOR,
			METHOD_PROPERTY_GENERATOR
		)
	);

	private static final CompositePropertyGenerator COMPOSITE_PROPERTY_GENERATOR =
		new CompositePropertyGenerator(
			Arrays.asList(
				ConstructorPropertiesArbitraryIntrospector.PROPERTY_GENERATOR,
				FIELD_METHOD_PROPERTY_GENERATOR
			)
		);

	public List<Property> generateChildProperties(Property property) {
		return COMPOSITE_PROPERTY_GENERATOR.generateChildProperties(property);
	}
}

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

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.InterfaceCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * It is deprecated. Use {@link InterfaceCandidateConcretePropertyResolver} instead.
 */
@API(since = "0.4.7", status = Status.MAINTAINED)
@Deprecated
public final class InterfaceObjectPropertyGenerator<T> implements ObjectPropertyGenerator {
	private final CandidateConcretePropertyResolver delegate;

	public InterfaceObjectPropertyGenerator(List<Class<? extends T>> implementations) {
		this.delegate = new InterfaceCandidateConcretePropertyResolver<>(implementations);
	}

	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property interfaceProperty = context.getProperty();
		double nullInject = context.getNullInjectGenerator().generate(context);

		PropertyGenerator propertyGenerator = context.getPropertyGenerator();

		Map<Property, List<Property>> childPropertiesByProperty = delegate.resolve(interfaceProperty).stream()
			.collect(
				toMap(
					Function.identity(),
					propertyGenerator::generateChildProperties
				)
			);

		return new ObjectProperty(
			interfaceProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childPropertiesByProperty
		);
	}

}

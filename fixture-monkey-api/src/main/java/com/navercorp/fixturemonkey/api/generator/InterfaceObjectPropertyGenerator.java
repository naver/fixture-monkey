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

import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;

@API(since = "0.4.7", status = Status.EXPERIMENTAL)
public final class InterfaceObjectPropertyGenerator<T> implements ObjectPropertyGenerator {
	private final List<Class<? extends T>> implementations;

	public InterfaceObjectPropertyGenerator(List<Class<? extends T>> implementations) {
		this.implementations = implementations;
	}

	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property interfaceProperty = context.getProperty();
		Class<?> implementation = implementations.get(Randoms.nextInt(implementations.size()));

		Property property = new Property() {
			@Override
			public Type getType() {
				return implementation;
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return generateAnnotatedTypeWithoutAnnotation(implementation);
			}

			@Nullable
			@Override
			public String getName() {
				return interfaceProperty.getName();
			}

			@Override
			public List<Annotation> getAnnotations() {
				return interfaceProperty.getAnnotations();
			}

			@Nullable
			@Override
			public Object getValue(Object obj) {
				return interfaceProperty.getValue(obj);
			}
		};

		double nullInject = context.getGenerateOptions().getNullInjectGenerator(property)
			.generate(context);

		List<Property> childProperties = context.getGenerateOptions().getPropertyGenerator(property)
			.generateObjectChildProperties(property.getAnnotatedType());

		return new ObjectProperty(
			property,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			childProperties
		);
	}
}

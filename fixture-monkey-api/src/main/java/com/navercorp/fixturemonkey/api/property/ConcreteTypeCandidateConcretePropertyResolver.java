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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.GenericType;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * This class is used to resolve more concrete types for a given interface.
 * The concrete types could be an interface or a class that implements the interface.
 *
 * @param <T> the type parameter of the interface
 */
@API(since = "1.0.16", status = Status.EXPERIMENTAL)
public final class ConcreteTypeCandidateConcretePropertyResolver<T> implements CandidateConcretePropertyResolver {
	private final List<Class<? extends T>> concreteTypes;

	public ConcreteTypeCandidateConcretePropertyResolver(List<Class<? extends T>> concreteTypes) {
		this.concreteTypes = concreteTypes;
	}

	/**
	 * Resolves more concrete types for a given interface.
	 * The concrete types could be an interface or a class that implements the interface.
	 * The provided property could be a property of concrete type or an abstract class or interface.
	 * The type parameter of the interface is used to generate properties of concrete types.
	 * It returns a list of properties of concrete types that implement the interface.
	 *
	 * @param property it could be a property of concrete type or an abstract class or interface.
	 * @return a list of properties of concrete types that implement the interface
	 */
	@Override
	@SuppressWarnings("type.argument.inference.crashed")
	public List<Property> resolve(Property property) {
		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(property.getAnnotatedType());

		if (!genericsTypes.isEmpty()) {
			Type[] typeArguments = genericsTypes.stream()
				.map(AnnotatedType::getType)
				.toArray(Type[]::new);

			return concreteTypes.stream()
				.map(it -> {
					Type concreteGenericType = new GenericType(it, typeArguments, null);

					AnnotatedType genericAnnotatedType = new AnnotatedType() {
						@Override
						public Type getType() {
							return concreteGenericType;
						}

						@Override
						public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
							return property.getAnnotation(annotationClass).orElse(null);
						}

						@Override
						public Annotation[] getAnnotations() {
							return property.getAnnotations().toArray(new Annotation[0]);
						}

						@Override
						public Annotation[] getDeclaredAnnotations() {
							return property.getAnnotations().toArray(new Annotation[0]);
						}
					};

					return new ConcreteTypeProperty(genericAnnotatedType, property);
				})
				.collect(Collectors.toList());
		}

		return concreteTypes.stream()
			.map(PropertyUtils::toProperty)
			.collect(Collectors.toList());
	}
}

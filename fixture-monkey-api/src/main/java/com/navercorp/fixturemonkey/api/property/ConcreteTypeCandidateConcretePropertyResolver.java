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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.16", status = Status.EXPERIMENTAL)
public final class ConcreteTypeCandidateConcretePropertyResolver<T> implements CandidateConcretePropertyResolver {
	private final List<Class<? extends T>> concreteTypes;

	public ConcreteTypeCandidateConcretePropertyResolver(List<Class<? extends T>> concreteTypes) {
		this.concreteTypes = concreteTypes;
	}

	@Override
	public List<Property> resolve(Property property) {
		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(property.getAnnotatedType());

		if (!genericsTypes.isEmpty()) {
			Type[] typeArguments = genericsTypes.stream()
				.map(AnnotatedType::getType)
				.toArray(Type[]::new);

			return concreteTypes.stream()
				.map(it -> {
					Type concreteGenericType = new ParameterizedType() {

						@Override
						public Type[] getActualTypeArguments() {
							return typeArguments;
						}

						@Override
						public Type getRawType() {
							return it;
						}

						@Override
						public Type getOwnerType() {
							return null;
						}
					};

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

					return new Property() {
						@Override
						public Type getType() {
							return genericAnnotatedType.getType();
						}

						@Override
						public AnnotatedType getAnnotatedType() {
							return genericAnnotatedType;
						}

						@Nullable
						@Override
						public String getName() {
							return property.getName();
						}

						@Override
						public List<Annotation> getAnnotations() {
							return Arrays.asList(genericAnnotatedType.getAnnotations());
						}

						@Override
						public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
							return Optional.ofNullable(genericAnnotatedType.getAnnotation(annotationClass));
						}

						@Nullable
						@Override
						public Object getValue(Object instance) {
							return property.getValue(instance);
						}

						@Override
						public int hashCode() {
							return getType().hashCode();
						}

						@Override
						public boolean equals(Object obj) {
							if (this == obj) {
								return true;
							}
							if (obj == null || getClass() == obj.getClass()) {
								return false;
							}

							Property that = (Property)obj;
							return getType().equals(that.getType());
						}
					};
				})
				.collect(Collectors.toList());
		}

		return concreteTypes.stream()
			.map(PropertyUtils::toProperty)
			.collect(Collectors.toList());
	}
}

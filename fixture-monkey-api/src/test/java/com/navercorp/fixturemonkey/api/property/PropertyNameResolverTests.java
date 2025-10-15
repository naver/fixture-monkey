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

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

class PropertyNameResolverTests {
	@Property
	void identityPropertyNameResolver(@ForAll String name) {
		PropertyNameResolver sut = PropertyNameResolver.IDENTITY;
		com.navercorp.fixturemonkey.api.property.Property property = getNameProperty(name);
		String actual = sut.resolve(property);
		then(actual).isEqualTo(property.getName());
	}

	private com.navercorp.fixturemonkey.api.property.Property getNameProperty(String name) {
		return new com.navercorp.fixturemonkey.api.property.Property() {
			@Override
			public Class<?> getType() {
				return null;
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return null;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<Annotation> getAnnotations() {
				return null;
			}

			@Nullable
			@Override
			public Object getValue(Object instance) {
				return null;
			}
		};
	}
}

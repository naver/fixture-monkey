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
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class PropertyTest {
	@Test
	void getAnnotation() {
		Property sut = this.anonymousProperty();
		then(sut.getAnnotation(Nullable.class)).isPresent();
	}

	@Test
	void getAnnotationNotFound() {
		Property sut = this.anonymousProperty();
		then(sut.getAnnotation(NonNull.class)).isEmpty();
	}

	private Property anonymousProperty() {
		return new Property() {
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
				return null;
			}

			@Override
			public List<Annotation> getAnnotations() {
				try {
					return Arrays.asList(PropertyValue.class.getDeclaredField("name").getAnnotations());
				} catch (NoSuchFieldException ex) {
					throw new RuntimeException(ex);
				}
			}

			@Nullable
			@Override
			public Object getValue(Object instance) {
				return null;
			}
		};
	}
}

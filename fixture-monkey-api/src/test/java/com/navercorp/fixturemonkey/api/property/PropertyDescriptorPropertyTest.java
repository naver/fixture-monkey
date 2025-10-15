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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class PropertyDescriptorPropertyTest {
	@Test
	void getPropertyDescriptor() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getPropertyDescriptor()).isSameAs(propertyDescriptor);
	}

	@Test
	void getAnnotations() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getAnnotations()).hasSize(1);
		then(sut.getAnnotations().get(0).annotationType()).isEqualTo(NonNull.class);
	}

	@Test
	void getAnnotation() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getAnnotation(NonNull.class)).isPresent();
	}

	@Test
	void getAnnotationNotFound() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getAnnotation(Nullable.class)).isEmpty();
	}

	@Test
	void getName() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getName()).isEqualTo("name");
	}

	@Test
	void getAnnotatedType() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getAnnotatedType().getType()).isEqualTo(String.class);
	}

	@Test
	void getValue() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		PropertyValue propertyValue = new PropertyValue("hello world");
		then(sut.getValue(propertyValue)).isEqualTo("hello world");
	}

	private PropertyDescriptor getNamePropertyDescriptor() {
		try {
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(PropertyValue.class).getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				if (descriptor.getName().equals("name")) {
					return descriptor;
				}
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}

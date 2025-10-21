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
import java.lang.reflect.Field;

import javax.annotation.CheckForNull;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class CompositePropertyTest {
	@Test
	void getProperty() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getPrimaryProperty()).isSameAs(primaryProperty);
		then(sut.getSecondaryProperty()).isSameAs(secondaryProperty);
	}

	@Test
	void getAnnotations() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getAnnotations()).hasSize(2);
		then(sut.getAnnotations().get(0).annotationType()).isEqualTo(NonNull.class);
		then(sut.getAnnotations().get(1).annotationType()).isEqualTo(Nullable.class);
	}

	@Test
	void getAnnotation() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getAnnotation(NonNull.class)).isPresent();
		then(sut.getAnnotation(Nullable.class)).isPresent();
	}

	@Test
	void getAnnotationNotFound() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getAnnotation(CheckForNull.class)).isEmpty();
	}

	@Test
	void getName() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getName()).isEqualTo("name");
	}

	@Test
	void getAnnotatedType() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		then(sut.getAnnotatedType().getType()).isEqualTo(String.class);
	}

	@Test
	void getValue() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty sut = new CompositeProperty(primaryProperty, secondaryProperty);

		PropertyValue propertyValue = new PropertyValue("hello world");
		then(sut.getValue(propertyValue)).isEqualTo("hello world");
	}

	@Test
	void equalsAndHashCode() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty primaryProperty = new PropertyDescriptorProperty(propertyDescriptor);
		Field field = getNameField();
		FieldProperty secondaryProperty = new FieldProperty(field);

		CompositeProperty property1 = new CompositeProperty(primaryProperty, secondaryProperty);
		CompositeProperty property2 = new CompositeProperty(primaryProperty, secondaryProperty);

		then(property1.equals(property2)).isTrue();
		then(property1.hashCode() == property2.hashCode()).isTrue();
	}

	private Field getNameField() {
		try {
			return PropertyValue.class.getDeclaredField("name");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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

package com.navercorp.fixturemonkey.api.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
		then(sut.getAnnotations().get(0).annotationType()).isEqualTo(Nonnull.class);
	}

	@Test
	void getAnnotation() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty sut = new PropertyDescriptorProperty(propertyDescriptor);
		then(sut.getAnnotation(Nonnull.class)).isPresent();
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

	@Test
	void equalsAndHashCode() {
		PropertyDescriptor propertyDescriptor = getNamePropertyDescriptor();
		PropertyDescriptorProperty property1 = new PropertyDescriptorProperty(propertyDescriptor);
		PropertyDescriptorProperty property2 = new PropertyDescriptorProperty(propertyDescriptor);
		then(property1.equals(property2)).isTrue();
		then(property1.hashCode() == property2.hashCode()).isTrue();
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

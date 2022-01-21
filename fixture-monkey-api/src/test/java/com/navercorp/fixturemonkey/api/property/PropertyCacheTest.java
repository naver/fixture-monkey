package com.navercorp.fixturemonkey.api.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class PropertyCacheTest {
	@Test
	void getReadProperty() {
		Optional<Property> actual = PropertyCache.getReadProperty(PropertyValue.class, "name");
		then(actual).isPresent();
		then(actual.get()).isExactlyInstanceOf(CompositeProperty.class);

		CompositeProperty compositeProperty = (CompositeProperty)actual.get();
		then(compositeProperty.getPrimaryProperty()).isExactlyInstanceOf(PropertyDescriptorProperty.class);
		then(compositeProperty.getSecondaryProperty()).isExactlyInstanceOf(FieldProperty.class);
	}

	@Test
	void getReadPropertyEmpty() {
		Optional<Property> actual = PropertyCache.getReadProperty(PropertyValue.class, "test");
		then(actual).isNotPresent();
	}

	@Test
	void getReadPropertyByMethod() throws NoSuchMethodException {
		Method method = PropertyValue.class.getDeclaredMethod("getName");
		Optional<Property> actual = PropertyCache.getReadProperty(PropertyValue.class, method);
		then(actual).isPresent();
		then(actual.get()).isExactlyInstanceOf(CompositeProperty.class);

		CompositeProperty compositeProperty = (CompositeProperty)actual.get();
		then(compositeProperty.getPrimaryProperty()).isExactlyInstanceOf(PropertyDescriptorProperty.class);
		then(compositeProperty.getSecondaryProperty()).isExactlyInstanceOf(FieldProperty.class);
	}

	@Test
	void getFields() {
		Map<String, Field> actual = PropertyCache.getFields(PropertyValue.class);
		then(actual).hasSize(1);
		then(actual.get("name")).isNotNull();
		then(actual.get("name").getName()).isEqualTo("name");
	}

	@Test
	void getReadPropertyDescriptors() throws NoSuchMethodException {
		Map<Method, PropertyDescriptor> actual = PropertyCache.getReadPropertyDescriptors(PropertyValue.class);
		then(actual).hasSize(2);

		Method method = PropertyValue.class.getDeclaredMethod("getName");
		then(actual.get(method)).isNotNull();
		then(actual.get(method).getName()).isEqualTo("name");
	}
}

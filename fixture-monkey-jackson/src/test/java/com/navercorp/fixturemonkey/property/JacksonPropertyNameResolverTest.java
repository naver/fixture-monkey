package com.navercorp.fixturemonkey.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

class JacksonPropertyNameResolverTest {
	@Test
	void resolve() throws NoSuchFieldException {
		PropertyNameResolver sut = new JacksonPropertyNameResolver();
		Field field = JacksonSample.class.getDeclaredField("name");
		Property property = new FieldProperty(field);
		then(sut.resolve(property)).isEqualTo("name");
	}

	@Test
	void resolveWithJsonProperty() throws NoSuchFieldException {
		PropertyNameResolver sut = new JacksonPropertyNameResolver();
		Field field = JacksonSample.class.getDeclaredField("address");
		Property property = new FieldProperty(field);
		then(sut.resolve(property)).isEqualTo("baseAddress");
	}

	static class JacksonSample {
		private String name;

		@JsonProperty("baseAddress")
		private String address;
	}
}
